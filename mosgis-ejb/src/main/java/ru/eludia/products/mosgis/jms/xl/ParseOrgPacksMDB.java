package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import ru.eludia.base.DB;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlOrgPackItem;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.jms.xl.base.XLMDB;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlOrgPacksQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ParseOrgPacksMDB extends XLMDB {
    
    @EJB
    public UUIDPublisher uuidPublisher;
    
    @Resource (mappedName = "mosgis.inXlOrgPackCheckQueue")
    Queue inXlOrgPackCheckQueue;        
       
    private static final int N_COL_ERR  = 2;
//    private static final int N_COL_UUID  = 7;

    protected void addLines (XSSFSheet sheet, UUID parent, DB db) throws SQLException {

        for (int i = 1; i <= sheet.getLastRowNum (); i ++) {
            
            final XSSFRow row = sheet.getRow (i);

            if (row.getCell (0) == null) continue;
            
            db.insert (InXlOrgPackItem.class, InXlOrgPackItem.toHash (parent, i, row));
           
        }
        
    }   

    private void checkLines (XSSFSheet sheet, DB db, UUID parent) throws SQLException {
        
        List<Map<String, Object>> lines = db.getList (db.getModel ()
            .select (InXlOrgPackItem.class, "*")
            .where (InXlOrgPackItem.c.UUID_XL, parent)
        );
                
        for (Map<String, Object> line: lines) {
            XSSFRow row = sheet.getRow ((int) DB.to.Long (line.get (InXlOrgPackItem.c.ORD.lc ())));
            XSSFCell cell = row.getLastCellNum () - 1 < N_COL_ERR ? row.createCell (N_COL_ERR) : row.getCell (N_COL_ERR);
            final String err = line.get (InXlOrgPackItem.c.ERR.lc ()).toString ();            
            cell.setCellValue (DB.ok (err) ? err : "Принято к отправке запроса в ГИС");
        }
        
    }

    @Override
    protected void completeOK (DB db, UUID parent, XSSFWorkbook wb) throws SQLException {        
        super.completeOK (db, parent, wb);
        setStatus (db, parent, VocFileStatus.i.PROCESSING);
        uuidPublisher.publish (inXlOrgPackCheckQueue, parent);
        //...
    }

    @Override
    protected void completeFail (DB db, UUID parent, XSSFWorkbook wb) throws SQLException {       
        super.completeFail (db, parent, wb);
    }

    @Override
    protected void processLines (XSSFWorkbook wb, UUID uuid, DB db) throws Exception {
                
        final XSSFSheet sheetPayments = wb.getSheet ("Шаблон добавления организаций");        
        addLines (sheetPayments, uuid, db);        
        checkLines (sheetPayments, db, uuid);

    }

}
