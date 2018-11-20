package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.TextMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportHouseQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportHouseMDB extends TextMDB {

    private static final Logger logger = Logger.getLogger (ExportHouseMDB.class.getName ());

    @EJB
    private UUIDPublisher UUIDPublisher;

    @EJB
    private WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @Resource (mappedName = "mosgis.outExportHouseQueue")
    private Queue outExportHouseQueue;

    @Override
    protected void onTextMessage (TextMessage message) throws SQLException, JMSException {

        String fiasHouseGuid = message.getText ();

        logger.log (Level.INFO, "Got text: ''{0}''", fiasHouseGuid);

        if (fiasHouseGuid == null || "null".equals (fiasHouseGuid)) {
            logger.warning ("Empty FIASHouseGUID passed, ignoring");
            return;
        }
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            AckRequest.Ack ack = wsGisHouseManagementClient.exportHouseData(fiasHouseGuid);
            
            db.update (OutSoap.class, DB.HASH (
                "uuid",     ack.getRequesterMessageGUID (),
                "uuid_ack", ack.getMessageGUID ()
            ));
            
            UUIDPublisher.publish (outExportHouseQueue, UUID.fromString (ack.getRequesterMessageGUID ()));
            
        }
        catch (SQLException | Fault ex) {
            logger.log (Level.SEVERE, null, ex);
        }
        
    }

}
