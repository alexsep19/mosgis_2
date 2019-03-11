package ru.eludia.products.mosgis.jms.gis.send;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.InVocDiff;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisTariffClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportDiffQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportVocDiffMDB extends UUIDMDB<InVocDiff> {
    
    private static final Logger logger = Logger.getLogger (ExportVocDiffMDB.class.getName ());

    @EJB
    protected UUIDPublisher UUIDPublisher;

    @EJB
    protected WsGisTariffClient wsGisTariffClient;

    @Resource (mappedName = "mosgis.outExportDiffQueue")
    Queue outExportDiffQueue;
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map r) throws Exception {

        AckRequest.Ack ack = wsGisTariffClient.exportTariffDifferentiation (null, uuid);
            
        db.update (OutSoap.class, DB.HASH (
            "uuid",     uuid,
            "uuid_ack", ack.getMessageGUID ()
        ));
                
        UUIDPublisher.publish (outExportDiffQueue, uuid);

    }

}