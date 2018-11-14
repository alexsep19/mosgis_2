package ru.eludia.products.mosgis.jms.gis.ttl;

import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterLog;
import ru.eludia.products.mosgis.db.model.tables.StuckCharters;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.stuckChartersQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class StuckChartersMDB extends StuckMDB<StuckCharters> {

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws Exception {               
        
        final Map<String, Object> values = getValues (db, uuid, r);
        
        db.update (Charter.class, values);
        
        if (values.containsKey ("uuid_out_soap")) db.update (CharterLog.class, DB.HASH (
            "uuid", r.get ("id_log"),
            "uuid_out_soap", values.get ("uuid_out_soap")
        ));        
        
    }
    
}
