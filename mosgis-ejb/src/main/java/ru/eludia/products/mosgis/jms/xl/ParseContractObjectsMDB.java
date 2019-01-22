package ru.eludia.products.mosgis.jms.xl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.InXlFile;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws Exception {

        XSSFWorkbook wb = readWorkbook (db, uuid);
        XSSFSheet s1 = wb.getSheetAt (0);

        logger.info ("s1.getFirstRowNum ()" + s1.getFirstRowNum ());
        logger.info ("s1.getLastRowNum ()" + s1.getLastRowNum ());

    }

}
