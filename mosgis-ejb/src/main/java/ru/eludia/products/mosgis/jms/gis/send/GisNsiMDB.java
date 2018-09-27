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
import ru.eludia.products.mosgis.db.model.incoming.nsi.InNsiGroup;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocNsiListGroup;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisNsiCommonClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inNsiQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisNsiMDB extends UUIDMDB<InNsiGroup> {
    
    private static final Logger logger = Logger.getLogger (GisNsiMDB.class.getName ());

    @EJB
    protected UUIDPublisher UUIDPublisher;

    @EJB
    protected WsGisNsiCommonClient wsGisNsiCommonClient;

    @Resource (mappedName = "mosgis.outExportNsiQueue")
    Queue outExportNsiQueue;
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map r) throws SQLException {
        
        String g = r.get ("listgroup").toString ();
        
        try {
            
            AckRequest.Ack ack = wsGisNsiCommonClient.exportNsiList (VocNsiListGroup.i.forName (g), uuid);
            
            db.update (OutSoap.class, DB.HASH (
                "uuid",     uuid,
                "uuid_ack", ack.getMessageGUID ()
            ));
                
            UUIDPublisher.publish (outExportNsiQueue, UUID.fromString (ack.getRequesterMessageGUID ()));
            
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot import NSI group " + g, ex);
        }

    }

}