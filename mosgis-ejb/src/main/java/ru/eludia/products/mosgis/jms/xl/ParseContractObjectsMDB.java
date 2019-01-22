package ru.eludia.products.mosgis.jms.xl;

import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.InXlFile;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlContractObjectsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ParseContractObjectsMDB extends UUIDMDB<InXlFile> {

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws Exception {
        
        logger.info ("XXXXXLLLLLL: " + DB.to.json (r));
        
    }
    
}
