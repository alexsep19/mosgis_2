package ru.eludia.products.mosgis.jms.fs;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.InVocBic;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.jmx.Conf;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inBicQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class InBicMDB extends UUIDMDB<InVocBic> {

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws Exception {
        
        URL url = new URL (Conf.get (VocSetting.i.URL_BIC));
        
        try (InputStream is = url.openStream ()) {
            
            final List<Map<String, Object>> records = VocBic.parseZipInputStream (is);
            
            for (Map<String, Object> i: records) {
                db.upsert (VocBic.class, i);
//                logger.info ("BIC record: " + i);
            }
            
//            db.upsert (VocBic.class, records);
            
        }

    }
    
}