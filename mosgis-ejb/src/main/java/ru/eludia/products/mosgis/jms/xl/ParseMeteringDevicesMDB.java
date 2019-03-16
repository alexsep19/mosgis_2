package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import ru.eludia.base.DB;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlMeteringDevice;
import ru.eludia.products.mosgis.db.model.tables.MeteringDevice;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi2;
import ru.eludia.products.mosgis.jms.xl.base.XLMDB;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlContractObjectsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ParseMeteringDevicesMDB extends XLMDB {
        
    protected void addMeters (XSSFSheet sheet, UUID parent, DB db, Map<Integer, Integer> resourceMap) throws SQLException {

        for (int i = 2; i <= sheet.getLastRowNum (); i ++) {
            
            UUID uuid = (UUID) db.insertId (InXlMeteringDevice.class, InXlMeteringDevice.toHash (parent, i, sheet.getRow (i)));
            
            try {
                
                db.update (InXlMeteringDevice.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    EnTable.c.IS_DELETED, 0
                ));
                
            }
            catch (SQLException e) {

                String s = e.getMessage ();

                if (e.getErrorCode () == 20000) s =
                    new StringTokenizer (e.getMessage (), "\n\r")
                    .nextToken ()
                    .replace ("ORA-20000: ", "");

                db.update (InXlMeteringDevice.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    InXlMeteringDevice.c.ERR, s
                ));
                
            }
            
        }
        
    }   

    private boolean checkMeterLines (XSSFSheet sheet, DB db, UUID parent) throws SQLException {
        
        List<Map<String, Object>> brokenLines = db.getList (db.getModel ()
            .select (InXlMeteringDevice.class, "*")
            .where (InXlMeteringDevice.c.UUID_XL, parent)
            .where (EnTable.c.IS_DELETED, 1)
        );
        
        if (brokenLines.isEmpty ()) return true;
        
        for (Map<String, Object> brokenLine: brokenLines) {
            XSSFRow row = sheet.getRow ((int) DB.to.Long (brokenLine.get (InXlMeteringDevice.c.ORD.lc ())));
            XSSFCell cell = row.getLastCellNum () < 9 ? row.createCell (9) : row.getCell (9);
            cell.setCellValue (brokenLine.get (InXlMeteringDevice.c.ERR.lc ()).toString ());
        }

        return false;
        
    }

    @Override
    protected void completeOK (DB db, UUID parent) throws SQLException {
        
        super.completeOK (db, parent);
        
        db.update (MeteringDevice.class, DB.HASH (
            MeteringDevice.c.UUID_XL, parent,
            EnTable.c.IS_DELETED, 0
        ), MeteringDevice.c.UUID_XL.lc ());

    }

    @Override
    protected void completeFail (DB db, UUID parent, XSSFWorkbook wb) throws SQLException {
        
        super.completeFail (db, parent, wb);
        
        db.delete (db.getModel ()
            .select (MeteringDevice.class)
            .where  (MeteringDevice.c.UUID_XL, parent)
        );            
        
    }
    
    static Map<Integer, Integer> toResourceMap (XSSFSheet sheet) throws XLException {
        
        final HashMap<Integer, Integer> result = new HashMap<> ();
        
        for (int i = 2; i <= sheet.getLastRowNum (); i ++) {
            
            XSSFRow row = sheet.getRow (i);
            
            final int k = EnTable.toNumeric (row, 0, "Некорректный ссылочный номер").intValue ();

            result.put (k, 
                (int) DB.to.Long (result.get (k)) + 
                Nsi2.i.forLabel (EnTable.toString (row, 2)).getId ()
            );

        }

        return result;

    }
    
    @Override
    protected void processLines (XSSFWorkbook wb, UUID uuid, DB db) throws Exception {
        
        boolean isOk = true;        
        
        final XSSFSheet sheetAddResources = wb.getSheetAt (1);
        Map<Integer, Integer> resourceMap = toResourceMap (sheetAddResources);
        
System.out.println ("resourceMap=" + resourceMap);

        final XSSFSheet sheetMeters = wb.getSheetAt (0);        
        addMeters (sheetMeters, uuid, db, resourceMap);
        
        if (!checkMeterLines (sheetMeters, db, uuid)) isOk = false;

        if (!isOk) throw new XLException ();

    }

}
