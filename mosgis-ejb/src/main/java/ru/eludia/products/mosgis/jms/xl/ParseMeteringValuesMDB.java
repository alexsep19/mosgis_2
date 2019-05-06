package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
       
    private static final int N_COL_ERR  = 7;

    protected void addValues (XSSFSheet sheet, UUID parent, DB db) throws SQLException {

        Map<String,Date> hashDevices = new HashMap<String,Date>();
        
        for (int i = 1; i <= sheet.getLastRowNum (); i ++) {
            
            final XSSFRow row = sheet.getRow (i);

            if (row.getCell (1) == null) continue;
            
            Map<String, Object> xlMeteringHash = InXlMeteringValues.toHash (parent, i, row, null);
            checkEqualsDateForSamePU( xlMeteringHash, hashDevices);
            UUID uuid = (UUID) db.insertId (InXlMeteringValues.class, xlMeteringHash);            
            try{
                updateDeviceValues(uuid, db);
            }catch(SQLException e){
                setCellStringValue (row, N_COL_ERR, e.getMessage());
            }    
        }
        
    }   

    //Если в ПУ несколько ресурсов, то для всех ресурсов дата должна совпадать
    private static void checkEqualsDateForSamePU(Map<String, Object> xlMeteringHash, Map<String,Date> hashDevices) throws SQLException{
        String deviceUuid = (String) xlMeteringHash.get(InXlMeteringValues.c.DEVICE_NUMBER);
        Date dateValue = (Date) xlMeteringHash.get(InXlMeteringValues.c.DATEVALUE.lc ());
        Date hashDate;
        if ((hashDate = hashDevices.get(deviceUuid)) == null){
            hashDevices.put(deviceUuid, dateValue);
        }else if (hashDate.compareTo(dateValue) != 0){
            String err = "Для ПУ " + deviceUuid + " дата " + dateValue + " отлична от предыдущих";
            if (xlMeteringHash.get(InXlMeteringValues.c.ERR.lc ()) == null){
                xlMeteringHash.put (InXlMeteringValues.c.ERR.lc (), err);
            }else{
                xlMeteringHash.put (InXlMeteringValues.c.ERR.lc (), xlMeteringHash.get(InXlMeteringValues.c.ERR.lc ()) + "; " + err);
            }    
        }
    }
    
    public static void updateDeviceValues(UUID uuid, DB db) throws SQLException{
        
        try{
                db.update (InXlMeteringValues.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    EnTable.c.IS_DELETED, 0
                ));
                
                db.update (MeteringDeviceValue.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    EnTable.c.IS_DELETED, 0
                ));
                
            }
            catch (SQLException e) {

                String s = getErrorMessage (e);

                db.update (InXlMeteringValues.class, DB.HASH (
                    EnTable.c.UUID,           uuid,
                    InXlMeteringValues.c.ERR, s
                ));
                throw new SQLException(s);
            }
    }
    
//Если в ПУ несколько ресурсов, то для всех строк дата должна совпадать
//    public static void checkEqualsDateForSamePU(DB db, UUID parent) throws SQLException{
//        List<Map<String, Object>> lines = db.getList (db.getModel ()
//            .select (InXlMeteringValues.class, "*")
//            .where (InXlMeteringValues.c.UUID_XL, parent)
//            .orderBy(InXlMeteringValues.c.UUID_METER)
//        );
//        Map<String,Date> hash = new HashMap<String,Date>();
//        Date hashDate;
//        for(Map<String, Object> line: lines) {
//            String deviceUuid = (String) line.get(InXlMeteringValues.c.UUID_METER.lc ());
//            Date dateValue = (Date) line.get(InXlMeteringValues.c.DATEVALUE.lc ());
//            if ((hashDate = hash.get(deviceUuid)) == null){
//                hash.put(deviceUuid, dateValue);
//            }else if (hashDate.compareTo(dateValue) != 0){
//                String err = "Для ПУ " + deviceUuid + " дата " + dateValue + " отлична от предыдущих";
//                db.update (InXlMeteringValues.class, DB.HASH (
//                    EnTable.c.UUID,  line.get(EnTable.c.UUID.lc ()),
//                    InXlMeteringValues.c.ERR, err
//                ));
//                throw new SQLException(err);
//            }
//        }
//    }
    
    private boolean checkMeterLines (XSSFSheet sheet, DB db, UUID parent) throws SQLException {
        
        List<Map<String, Object>> brokenLines = db.getList (db.getModel ()
            .select (InXlMeteringValues.class, "*")
            .where (InXlMeteringValues.c.UUID_XL, parent)
            .where (EnTable.c.IS_DELETED, 1)
        );
        
        if (brokenLines.isEmpty ()) return true;
        
        for (Map<String, Object> brokenLine: brokenLines) {
            XSSFRow row = sheet.getRow ((int) DB.to.Long (brokenLine.get (InXlMeteringValues.c.ORD.lc ())));
            XSSFCell cell = row.getLastCellNum () <= N_COL_ERR ? row.createCell (N_COL_ERR) : row.getCell (N_COL_ERR);
            if (cell == null) cell = row.createCell (N_COL_ERR);
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
