package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceValueType;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi2;
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
    private static final int N_COL_DOP_ERR  = 5;
    public static final Date NULL_DATE = new Date(0);

    protected void addMeters (XSSFSheet sheet, UUID parent, DB db, Map<Integer, Integer> resourceMap, HashSet<Integer> refNums) throws SQLException {
        Date dateValue = NULL_DATE;
        Date datePeriod = NULL_DATE;   
        for (int i = 2; i <= sheet.getLastRowNum (); i ++) {
            
            final XSSFRow row = sheet.getRow (i);

            if (EnTable.isEmpty(row, 1)) continue;
            
            Map<String, Object> hashMeteringDevice = InXlMeteringDevice.toHash (parent, i, row, resourceMap, refNums);
            UUID uuidMeteringDevice = (UUID) db.insertId (InXlMeteringDevice.class, hashMeteringDevice);    
            if ((Integer)hashMeteringDevice.get(InXlMeteringDevice.c.CONSUMEDVOLUME.lc ()) == 0){
               UUID uuidMeteringValues = (UUID) db.insertId (InXlMeteringValues.class, 
                       InXlMeteringValues.toHash (parent, i, row, dateValue, datePeriod, 
                          ( fr, frow, fdateValue, fdatePeriod)->{
                           //UUID_METER устанавливается в триггере
                           fr.put (InXlMeteringValues.c.DEVICE_NUMBER.lc (),    uuidMeteringDevice);  
                           fr.put (InXlMeteringValues.c.ID_TYPE.lc (),          VocMeteringDeviceValueType.i.BASE);
                           fr.put (InXlMeteringValues.c.CODE_VC_NSI_2.lc (),    Nsi2.i.forLabel (EnTable.toString (frow, 16, "Не указан коммунальный ресурс")).getId ());
                           //T 
                           fr.put (InXlMeteringValues.c.METERINGVALUET1.lc (),  EnTable.toNumeric (frow, 19, "Не указано значение показания (Т1)"));
                           fr.put (InXlMeteringValues.c.METERINGVALUET2.lc (),  EnTable.toNumeric (frow, 20 ));
                           fr.put (InXlMeteringValues.c.METERINGVALUET3.lc (),  EnTable.toNumeric (frow, 21 ));
                           fr.put (InXlMeteringValues.c.DATEVALUE.lc (),        InXlMeteringValues.processDateDevice((Date) EnTable.toDate(frow, 6, "Не указана дата снятия показания"), fdateValue, fdatePeriod));
                           fr.put (InXlMeteringValues.c.DT_PERIOD.lc (),        fdatePeriod);
                           return 0;
                       }));            
            }

            try {
                
                db.update (InXlMeteringDevice.class, DB.HASH (
                    EnTable.c.UUID, uuidMeteringDevice,
                    EnTable.c.IS_DELETED, 0
                ));
                
                db.update (MeteringDevice.class, DB.HASH (
                    EnTable.c.UUID, uuidMeteringDevice,
                    EnTable.c.IS_DELETED, 0
                ));
                
                setCellStringValue (row, N_COL_UUID, uuidMeteringDevice.toString ());

            }
            catch (SQLException e) {

                String s = getErrorMessage (e);

                db.update (InXlMeteringDevice.class, DB.HASH (
                    EnTable.c.UUID, uuidMeteringDevice,
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

//        List<Map<String, Object>> brokenLines = db.getList (db.getModel ()
//            .select (InXlMeteringDevice.class, "AS m", "*")
//            .toMaybeOne (InXlMeteringValues.class, "AS v").on("v.uuid_xl = m.uuid_xl")
//            .where (InXlMeteringDevice.c.UUID_XL, parent)
//            .and ("m.is_deleted", 1).
//            .or("v.is_deleted", 1)
//        );
        
        if (brokenLines.isEmpty ()) return true;
        
        for (Map<String, Object> brokenLine: brokenLines) {
            XSSFRow row = sheet.getRow ((int) DB.to.Long (brokenLine.get (InXlMeteringDevice.c.ORD.lc ())));
            XSSFCell cell = row.getLastCellNum () <= N_COL_ERR ? row.createCell (N_COL_ERR) : row.getCell (N_COL_ERR);
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
            if (EnTable.isEmpty(row, 0)) break;
            try{
              final int k = EnTable.toNumeric (row, 0, "Некорректный ссылочный номер").intValue ();
              result.put (k, InXlMeteringDevice.addResource (result, k, EnTable.toString (row, 2)));
            }catch(XLException e){
              writeErrorToXls(sheet, i, e.getMessage()); 
              throw new XLException (e.getMessage());
            }
        }

        return result;

    }
    
    @Override
    protected void processLines (XSSFWorkbook wb, UUID uuid, DB db) throws Exception {
        
        Map<Integer, Integer> resourceMap = null;
        HashSet<Integer> refNums = new HashSet<Integer>();
        
        final XSSFSheet sheetAddResources = wb.getSheet ("Доп. комм. ресурсы");        
        
        resourceMap = toResourceMap (sheetAddResources);
        
        final XSSFSheet sheetMeters = wb.getSheet ("Сведения о ПУ");        
        addMeters (sheetMeters, uuid, db, resourceMap, refNums);
        
        if (!checkMeterLines (sheetMeters, db, uuid) || !isContains(resourceMap, refNums, sheetAddResources)) 
            throw new XLException ();

    }

    private boolean isContains(Map<Integer, Integer> resourceMap, HashSet<Integer> refNums, XSSFSheet sheetAddResources){
        StringBuilder difference = new StringBuilder();
        resourceMap.forEach((k,v)-> {if (!refNums.contains(k)) difference.append(k).append(" ");});
        boolean result = difference.toString().isEmpty();
        if (!result){
            writeErrorToXls(sheetAddResources, 1, "На листе Сведения о ПУ не найдены ссылочные номера " + difference.toString());
        }
        return result;
    }
    
    static private void writeErrorToXls(XSSFSheet sheet, int numRow, String message){
        XSSFRow row = sheet.getRow(numRow);
        XSSFCell cell = row.getLastCellNum () <= N_COL_DOP_ERR  ? row.createCell (N_COL_DOP_ERR) : row.getCell (N_COL_DOP_ERR);
        cell.setCellValue (message);
    }
}
