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
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import ru.eludia.products.mosgis.db.model.voc.VocNsiListGroup;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisNsiCommonClient;
import ru.eludia.products.mosgis.jms.base.TextMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.nsi_common_service_async.Fault;

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

    @Resource (mappedName = "mosgis.outExportNsiItemQueue")
    Queue outExportNsiItemQueue;

    @Override
    protected void onTextMessage (TextMessage message) throws SQLException, JMSException {
        
        String text = message.getText ();
        
        logger.info ("Got text: '" + text + "'");
        
        if (text == null || "null".equals (text)) return;
        
        createImport (text).run ();

    }
    
    private Import createImport (String text) {
        
        VocNsiListGroup.i listGroup = VocNsiListGroup.i.forName (text);
            
        if (listGroup != null) return new ImportNsi (listGroup);
        
        int pos = text.indexOf ('.');
        
        if (pos < 0) return new ImportNsiItem (Integer.valueOf (text));

        return new ImportNsiPagingItem (Integer.valueOf (text.substring (0, pos)), Integer.valueOf (text.substring (pos + 1)));
        
    }
    
    abstract class Import implements Runnable {
        
        VocNsiListGroup.i listGroup;
        abstract AckRequest.Ack getAck () throws Fault;
        abstract Queue getQueue ();
                
        void registerAck (AckRequest.Ack ack, final DB db) throws SQLException {

            db.update (OutSoap.class, DB.HASH (
                "uuid",     ack.getRequesterMessageGUID (),
                "uuid_ack", ack.getMessageGUID ()
            ));

        }

        @Override
        public void run () {

            try (DB db = ModelHolder.getModel ().getDb ()) {
                AckRequest.Ack ack = getAck ();                
                registerAck (ack, db);                
                UUIDPublisher.publish (getQueue (), UUID.fromString (ack.getRequesterMessageGUID ()));
            }
            catch (SQLException ex) {
                logger.log (Level.SEVERE, null, ex);
            }
            catch (Fault ex) {
                logger.log (Level.SEVERE, null, ex);
            }

        }
        
    }
    
    private final class ImportNsi extends Import {
        
        public ImportNsi (VocNsiListGroup.i listGroup) {
            this.listGroup = listGroup;
        }

        @Override
        AckRequest.Ack getAck () throws Fault {
            return wsGisNsiCommonClient.exportNsiList (listGroup);
        }

        @Override
        Queue getQueue () {
            return outExportNsiQueue;
        }
        
    }
        
    private class ImportNsiItem extends Import {
        
        int registryNumber;

        public ImportNsiItem (int registryNumber) {

            this.registryNumber = registryNumber;
            
            try (DB db = ModelHolder.getModel ().getDb ()) {
                listGroup = VocNsiListGroup.i.forName (db.getString (VocNsiList.class, registryNumber, "listgroup"));
            }
            catch (SQLException ex) {
                throw new IllegalArgumentException (ex);
            }            
            
        }

        @Override
        AckRequest.Ack getAck () throws Fault {
            return wsGisNsiCommonClient.exportNsiItem (listGroup, registryNumber);
        }

        @Override
        final void registerAck (AckRequest.Ack ack, DB db) throws SQLException {

            db.begin ();
            
                super.registerAck (ack, db);

                db.update (VocNsiList.class, DB.HASH (
                    "registrynumber", registryNumber,
                    "uuid_out_soap", ack.getRequesterMessageGUID ()
                ));
            
            db.commit ();

        }

        @Override
        final Queue getQueue () {
            return outExportNsiItemQueue;
        }
        
    }
    
    private final class ImportNsiPagingItem extends ImportNsiItem {
        
        int page;

        public ImportNsiPagingItem (int registryNumber, int page) {
            super (registryNumber);
            this.page = page;
        }

        @Override
        AckRequest.Ack getAck () throws Fault {
            return wsGisNsiCommonClient.exportNsiPagingItem (listGroup, registryNumber, page);
        }
        
    }

}