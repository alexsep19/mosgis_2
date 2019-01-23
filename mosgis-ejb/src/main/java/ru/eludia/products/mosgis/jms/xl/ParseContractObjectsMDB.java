package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import ru.eludia.base.DB;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlContractObject;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.jms.xl.base.XLMDB;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlContractObjectsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ParseContractObjectsMDB extends XLMDB {
        
    protected void addObjectLines (XSSFSheet sheet, UUID parent, DB db) throws SQLException {

        for (int i = 2; i <= sheet.getLastRowNum (); i ++) {
            
            UUID uuid = (UUID) db.insertId (InXlContractObject.class, InXlContractObject.toHash (parent, i, sheet.getRow (i)));
            
            try {
                
                db.update (InXlContractObject.class, DB.HASH (
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

                db.update (InXlContractObject.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    InXlContractObject.c.ERR, s
                ));
                
            }
            
        }
        
    }

    private boolean checkObjectLines (XSSFSheet sheet, DB db, UUID parent) throws SQLException {
        
        List<Map<String, Object>> brokenLines = db.getList (db.getModel ()
            .select (InXlContractObject.class, "*")
            .where (EnTable.c.IS_DELETED, 1)
        );
        
        if (brokenLines.isEmpty ()) return true;            
            
        
        return false;
        
    }

    @Override
    protected void completeOK (DB db, UUID parent) throws SQLException {
        
        super.completeOK (db, parent);
        
        db.update (ContractObject.class, DB.HASH (
            InXlContractObject.c.UUID_XL, parent,
            EnTable.c.IS_DELETED, 0
        ), InXlContractObject.c.UUID_XL.lc ());
        
    }

    @Override
    protected void completeFail (DB db, UUID parent) throws SQLException {
        
        super.completeFail (db, parent);
        
        db.delete (db.getModel ()
            .select (ContractObject.class, "uuid")
            .where (InXlContractObject.c.UUID_XL, parent)
        );            

    }
    
    protected void processLines (XSSFWorkbook wb, UUID uuid, DB db) throws Exception {
        
        boolean isOk = true;        
        
        final XSSFSheet sheetObjects = wb.getSheetAt (0);
        
        addObjectLines (sheetObjects, uuid, db);
        
        if (!checkObjectLines (sheetObjects, db, uuid)) isOk = false;
        
        if (!isOk) throw new XLException ();

    }

}
