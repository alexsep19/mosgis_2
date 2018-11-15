package ru.eludia.products.mosgis.jms.gis.ttl;

import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractLog;
import ru.eludia.products.mosgis.db.model.tables.StuckContracts;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.stuckContractsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class StuckContractsMDB extends StuckMDB<StuckContracts> {

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws Exception {
        
        final Map<String, Object> values = getValues (db, uuid, r);
        
        db.update (Contract.class, values);                
        
        if (values.containsKey ("uuid_out_soap")) db.update (ContractLog.class, DB.HASH (
            "uuid", r.get ("id_log"),
            "uuid_out_soap", values.get ("uuid_out_soap")
        ));
        
    }
    
}