package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.Date;
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
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlMeteringValues;
import ru.eludia.products.mosgis.db.model.tables.MeteringDevice;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceValue;
import ru.eludia.products.mosgis.jms.xl.base.XLMDB;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlMeteringValuesQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ParseMeteringValuesMDB extends XLMDB {
       
    private static final int N_COL_UUID = 33;
    private static final int N_COL_ERR  = 34;
    public static final Date NULL_DATE = new Date(0);

    protected void addValues (XSSFSheet sheet, UUID parent, DB db/*, Map<Integer, Integer> resourceMap*/) throws SQLException {

        Date dateValue = NULL_DATE;
        Date datePeriod = NULL_DATE;
        
        for (int i = 1; i <= sheet.getLastRowNum (); i ++) {
            
            final XSSFRow row = sheet.getRow (i);

            if (row.getCell (1) == null) continue;
            
            UUID uuid = (UUID) db.insertId (InXlMeteringValues.class, InXlMeteringValues.toHash (parent, i, row, dateValue, datePeriod));            

            try {
                
                db.update (InXlMeteringValues.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    EnTable.c.IS_DELETED, 0
                ));
                
                db.update (MeteringDeviceValue.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    EnTable.c.IS_DELETED, 0
                ));
                
                setCellStringValue (row, N_COL_UUID, uuid.toString ());

            }
            catch (SQLException e) {

                String s = getErrorMessage (e);

                db.update (InXlMeteringValues.class, DB.HASH (
                    EnTable.c.UUID,           uuid,
                    InXlMeteringValues.c.ERR, s
                ));
                
                setCellStringValue (row, N_COL_UUID, "");
                setCellStringValue (row, N_COL_ERR, s);
                
            }
            
        }
        
    }   

    private boolean checkMeterLines (XSSFSheet sheet, DB db, UUID parent) throws SQLException {
        
        List<Map<String, Object>> brokenLines = db.getList (db.getModel ()
            .select (InXlMeteringValues.class, "*")
            .where (InXlMeteringValues.c.UUID_XL, parent)
            .where (EnTable.c.IS_DELETED, 1)
        );
        
        if (brokenLines.isEmpty ()) return true;
        
        for (Map<String, Object> brokenLine: brokenLines) {
            XSSFRow row = sheet.getRow ((int) DB.to.Long (brokenLine.get (InXlMeteringValues.c.ORD.lc ())));
            XSSFCell cell = row.getLastCellNum () < 8 ? row.createCell (7) : row.getCell (7);
            if (cell == null) cell = row.createCell (7);
            cell.setCellValue (brokenLine.get (InXlMeteringValues.c.ERR.lc ()).toString ());
        }

        return false;
        
    }

//    @Override
//    protected void completeOK (DB db, UUID parent, XSSFWorkbook wb) throws SQLException {
//        
//        super.completeOK (db, parent, wb);
//        
//        db.update (MeteringDeviceValue.class, DB.HASH (
//            MeteringDeviceValue.c.UUID_XL, parent,
//            EnTable.c.IS_DELETED, 0
//        ), MeteringDeviceValue.c.UUID_XL.lc ());
//
//    }

    @Override
    protected void completeFail (DB db, UUID parent, XSSFWorkbook wb) throws SQLException {
        
        super.completeFail (db, parent, wb);
        
        db.delete (db.getModel ()
            .select (MeteringDeviceValue.class)
            .where  (MeteringDeviceValue.c.UUID_XL, parent)
        );            
        
    }
        
    @Override
    protected void processLines (XSSFWorkbook wb, UUID uuid, DB db) throws Exception {
        
        boolean isOk = true;        
        
        final XSSFSheet sheetValues = wb.getSheet ("Импорт показаний");        
        addValues (sheetValues, uuid, db);
        
        if (!checkMeterLines (sheetValues, db, uuid)) isOk = false;

        if (!isOk) throw new XLException ();

    }

}
