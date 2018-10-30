package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.InVocOrganization;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisOrgClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inOrgQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})

public class GisOrgMdb extends UUIDMDB<InVocOrganization> {

    private static final Logger logger = Logger.getLogger (GisOrgMdb.class.getName ());

    @EJB
    protected UUIDPublisher UUIDPublisher;

    @EJB
    protected WsGisOrgClient wsGisOrgClient;
    
    @Resource (mappedName = "mosgis.outExportOrgQueue")
    Queue outExportOrgQueue;    

    public static final Pattern RE = Pattern.compile ("\\d{13}(\\d\\d)?");

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        try {
            
            AckRequest.Ack ack = wsGisOrgClient.exportOrgRegistry (r.get ("ogrn").toString (), uuid);
            
            db.update (OutSoap.class, DB.HASH (
                "uuid",     uuid,
                "uuid_ack", ack.getMessageGUID ()
            ));
            
            UUIDPublisher.publish (outExportOrgQueue, uuid);
            
        }
        catch (Fault ex) {
            Logger.getLogger (GisOrgMdb.class.getName()).log (Level.SEVERE, null, ex);
        }

    }

}
