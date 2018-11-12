package ru.eludia.products.mosgis.jms.gis.ttl;

import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.StuckCharters;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.stuckChartersQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class StuckChartersMDB extends UUIDMDB<StuckCharters> {

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws Exception {

        db.update (Charter.class, HASH (
            "uuid", uuid,
            "id_ctr_status", VocGisStatus.i.FAILED_STATE.getId ()
        ));
        
        Object uuidOutSoap = r.get (StuckCharters.c.UUID_OUT_SOAP);
        
        if (uuidOutSoap != null) {
            
            db.update (OutSoap.class, HASH (
                "uuid", uuidOutSoap,
                "id_status", 3,
                "is_failed", 1,
                "err_code", "0",
                "err_text", "Операция прервана по истечении времени"
            ));
            
        }
        
    }
    
}
