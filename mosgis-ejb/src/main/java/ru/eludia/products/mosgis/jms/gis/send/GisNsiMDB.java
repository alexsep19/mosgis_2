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
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocNsiListGroup;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisNsiCommonClient;
import ru.eludia.products.mosgis.jms.base.TextMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inNsiQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisNsiMDB extends TextMDB {
    
    private static final Logger logger = Logger.getLogger (GisNsiMDB.class.getName ());

    @EJB
    protected UUIDPublisher UUIDPublisher;

    @EJB
    protected WsGisNsiCommonClient wsGisNsiCommonClient;

    @Resource (mappedName = "mosgis.outExportNsiQueue")
    Queue outExportNsiQueue;

    @Override
    protected void onTextMessage (TextMessage message) throws SQLException, JMSException {
                
        try (DB db = ModelHolder.getModel ().getDb ()) {
                
            AckRequest.Ack ack = wsGisNsiCommonClient.exportNsiList (VocNsiListGroup.i.forName (message.getText ()));
                
            db.update (OutSoap.class, DB.HASH (
                "uuid",     ack.getRequesterMessageGUID (),
                "uuid_ack", ack.getMessageGUID ()
            ));
                
            UUIDPublisher.publish (outExportNsiQueue, UUID.fromString (ack.getRequesterMessageGUID ()));
                
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, null, ex);
        }
       
    }

}