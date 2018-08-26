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
import ru.eludia.products.mosgis.db.model.tables.AdditionalService;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.AdditionalServiceLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisNsiClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inNsiAdditionalServicesQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportAdditionalServiceLogMDB extends UUIDMDB<AdditionalServiceLog> {

    private final Logger logger = Logger.getLogger (ExportAdditionalServiceLogMDB.class.getName ());

    @EJB
    WsGisNsiClient wsGisNsiClient;
    
    @Resource (mappedName = "mosgis.outExportNsiAdditionalServicesQueue")
    Queue queue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        Map<String, Object> data = db.getMap (
            ModelHolder.getModel ()
                .get   (AdditionalService.class, r.get ("uuid_object"))
                .toOne (VocOrganization.class, "AS org", "orgppaguid").on ()
        );

logger.info ("data = " + data);
        
        UUID orgPPAGuid = (UUID) data.get ("org.orgppaguid");        

        AckRequest.Ack ack;

        try {
            ack = wsGisNsiClient.importAdditionalServices (orgPPAGuid, r);
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, null, ex);
            return;
        }

        db.update (OutSoap.class, DB.HASH (
            "uuid",     ack.getRequesterMessageGUID (),
            "uuid_ack", ack.getMessageGUID ()
        ));

        db.update (getTable (), DB.HASH (
            "uuid",          uuid,
            "uuid_out_soap", ack.getRequesterMessageGUID (),
            "uuid_message",  ack.getMessageGUID ()
        ));
        
        UUIDPublisher.publish (queue, ack.getRequesterMessageGUID ());

    }

}