package ru.eludia.products.mosgis.jms.xl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlContractObject;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlContractObjectsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ParseContractObjectsMDB extends UUIDMDB<InXlFile> {
    
    protected XSSFWorkbook readWorkbook (DB db, UUID uuid) throws SQLException {
        
        XSSFWorkbook [] x = new XSSFWorkbook [] {null};
        
        db.forFirst (db.getModel ().get (getTable (), uuid, "body"), (rs) -> {
            
            try {
                x [0] = new XSSFWorkbook (rs.getBlob (1).getBinaryStream ());
            }
            catch (IOException ex) {
                throw new IllegalStateException (ex);
            }

        });
        
        return x [0];
        
    }
    
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
            
            List<Map<String, Object>> brokenLines = db.getList (db.getModel ()
                .select (InXlContractObject.class, "*")
                .where (EnTable.c.IS_DELETED, 1)
            );
            
            if (brokenLines.isEmpty ()) {
                
                db.update (ContractObject.class, DB.HASH (
                    InXlContractObject.c.UUID_XL, parent,
                    EnTable.c.IS_DELETED, 0
                ), InXlContractObject.c.UUID_XL.lc ());                
                
            }
            else {
                
                db.delete (db.getModel ()
                    .select (ContractObject.class, "uuid")
                    .where (InXlContractObject.c.UUID_XL, parent)
                );
                
            }

        }
        
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws Exception {

        XSSFWorkbook wb = readWorkbook (db, uuid);
        
        addObjectLines (wb.getSheetAt (0), uuid, db);

    }

}
