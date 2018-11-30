package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.InAccessRequests;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisOrgClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inAccessRequestQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})

public class ExportAccessRequestsMdb extends UUIDMDB<InAccessRequests> {

    private static final Logger logger = Logger.getLogger (ExportAccessRequestsMdb.class.getName ());

    @EJB
    protected UUIDPublisher UUIDPublisher;

    @EJB
    protected WsGisOrgClient wsGisOrgClient;
    
    @Resource (mappedName = "mosgis.outExportAccessRequestQueue")
    Queue outExportQueue;    

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        try {
            
            AckRequest.Ack ack = wsGisOrgClient.exportDelegatedAccess (DB.to.Long (r.get ("page")), uuid);
            
            db.update (OutSoap.class, DB.HASH (
                "uuid",     uuid,
                "uuid_ack", ack.getMessageGUID ()
            ));
            
            UUIDPublisher.publish (outExportQueue, uuid);
            
        }
        catch (Fault ex) {
            Logger.getLogger (ExportAccessRequestsMdb.class.getName()).log (Level.SEVERE, null, ex);
        }

    }

}
