package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import ru.eludia.base.DB;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlMeteringDevice;
import ru.eludia.products.mosgis.db.model.tables.MeteringDevice;
import ru.eludia.products.mosgis.jms.xl.base.XLMDB;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlMeteringDevicesQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ParseMeteringDevicesMDB extends XLMDB {
       
    private static final int N_COL_UUID = 33;
    private static final int N_COL_ERR  = 34;

    protected void addMeters (XSSFSheet sheet, UUID parent, DB db, Map<Integer, Integer> resourceMap) throws SQLException {

        for (int i = 2; i <= sheet.getLastRowNum (); i ++) {
            
            final XSSFRow row = sheet.getRow (i);

            if (row.getCell (0) == null) continue;
            
            UUID uuid = (UUID) db.insertId (InXlMeteringDevice.class, InXlMeteringDevice.toHash (parent, i, row, resourceMap));            

            try {
                
                db.update (InXlMeteringDevice.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    EnTable.c.IS_DELETED, 0
                ));
                
                db.update (MeteringDevice.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    EnTable.c.IS_DELETED, 0
                ));
                
                setCellStringValue (row, N_COL_UUID, uuid.toString ());

            }
            catch (SQLException e) {

                String s = getErrorMessage (e);

                db.update (InXlMeteringDevice.class, DB.HASH (
                    EnTable.c.UUID,           uuid,
                    InXlMeteringDevice.c.ERR, s
                ));
                
                setCellStringValue (row, N_COL_UUID, "");
                setCellStringValue (row, N_COL_ERR, s);
                
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
    protected void completeOK (DB db, UUID parent, XSSFWorkbook wb) throws SQLException {
        
        super.completeOK (db, parent, wb);
        
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
        
        for (int i = 1; i <= sheet.getLastRowNum (); i ++) {
            XSSFRow row = sheet.getRow (i);           
            final int k = EnTable.toNumeric (row, 0, "Некорректный ссылочный номер").intValue ();
            result.put (k, InXlMeteringDevice.addResource (result, k, EnTable.toString (row, 2)));
        }

        return result;

    }
    
    @Override
    protected void processLines (XSSFWorkbook wb, UUID uuid, DB db) throws Exception {
        
        boolean isOk = true;        
        
        final XSSFSheet sheetAddResources = wb.getSheet ("Доп. комм. ресурсы");        
        Map<Integer, Integer> resourceMap = toResourceMap (sheetAddResources);
        
        final XSSFSheet sheetMeters = wb.getSheet ("Сведения о ПУ");        
        addMeters (sheetMeters, uuid, db, resourceMap);
        
        if (!checkMeterLines (sheetMeters, db, uuid)) isOk = false;

        if (!isOk) throw new XLException ();

    }

}
