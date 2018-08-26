package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisOrgClient;
import ru.eludia.products.mosgis.jms.base.TextMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inOrgQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisOrgMdb extends TextMDB {

    private static final Logger logger = Logger.getLogger (GisOrgMdb.class.getName ());

    @EJB
    protected UUIDPublisher UUIDPublisher;

    @EJB
    protected WsGisOrgClient wsGisOrgClient;
    
    @Resource (mappedName = "mosgis.outExportOrgQueue")
    Queue outExportOrgQueue;    

    public static final Pattern RE = Pattern.compile ("\\d{13}(\\d\\d)?");

    @Override
    protected void onTextMessage (TextMessage message) throws SQLException, JMSException {

        String ogrn = message.getText ();

        logger.info ("Got text: '" + ogrn + "'");

        if (ogrn == null || "null".equals (ogrn)) {
            logger.warning ("Empty OGRN passed, ignoring");
            return;
        }
        
        if (!RE.matcher (ogrn).matches ()) {
            logger.warning ("Invalid OGRN passed, ignoring: '" + ogrn + "'");
            return;
        }
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            AckRequest.Ack ack = wsGisOrgClient.exportOrgRegistry (ogrn);
            
            db.update (OutSoap.class, DB.HASH (
                "uuid",     ack.getRequesterMessageGUID (),
                "uuid_ack", ack.getMessageGUID ()
            ));
            
            UUIDPublisher.publish (outExportOrgQueue, UUID.fromString (ack.getRequesterMessageGUID ()));
            
        }
        catch (SQLException ex) {
            logger.log (Level.SEVERE, null, ex);
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, null, ex);
        }
        
    }

}
