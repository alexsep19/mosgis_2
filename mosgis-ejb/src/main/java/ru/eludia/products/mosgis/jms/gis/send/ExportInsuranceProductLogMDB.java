package ru.eludia.products.mosgis.jms.gis.send;

import ru.eludia.products.mosgis.ws.rest.clients.tools.GisRestStream;
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
import ru.eludia.products.mosgis.db.model.tables.InsuranceProduct;
import ru.eludia.products.mosgis.db.model.tables.InsuranceProductLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.ws.rest.clients.RestGisFilesClient;
import ru.eludia.products.mosgis.ws.rest.clients.RestGisFilesClient.Context;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisBillsClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.bills_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inInsuranceProductsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportInsuranceProductLogMDB extends UUIDMDB<InsuranceProductLog> {

    private final Logger logger = Logger.getLogger (ExportInsuranceProductLogMDB.class.getName ());
    
    @EJB
    RestGisFilesClient restGisFilesClient;

    @EJB
    WsGisBillsClient wsGisBillsClient;

    @Resource (mappedName = "mosgis.outExportInsuranceProductsQueue")
    Queue queue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        Map<String, Object> data = db.getMap (
            ModelHolder.getModel ()
                .get   (InsuranceProduct.class, r.get ("uuid_object"))
                .toOne (VocOrganization.class, "AS org", "orgppaguid").on ()
        );
        
        UUID orgPPAGuid = (UUID) data.get ("org.orgppaguid");

        try (
            GisRestStream out = new GisRestStream (
                restGisFilesClient,
                Context.BILLS,
                orgPPAGuid, 
                r.get ("name").toString (), 
                Long.parseLong (r.get ("len").toString ()),
                (uploadId, attachmentHash) -> {
                    r.put ("attachmentguid", uploadId);
                    r.put ("attachmenthash", attachmentHash);
                    db.update (getTable (), r);
                }
            )
        ) {            
            db.getStream (ModelHolder.getModel ().get (getTable (), uuid, "body"), out);
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot upload file", ex);
            return;
        }                        

        try {
            
            AckRequest.Ack ack = wsGisBillsClient.setInsuranceProduct (orgPPAGuid, r);
            
            db.update (getTable (), DB.HASH (
                "uuid",          uuid,
                "uuid_out_soap", ack.getRequesterMessageGUID (),
                "uuid_message",  ack.getMessageGUID ()
            ));

            UUIDPublisher.publish (queue, ack.getRequesterMessageGUID ());

        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, null, ex);
            return;
        }

    }
    

}