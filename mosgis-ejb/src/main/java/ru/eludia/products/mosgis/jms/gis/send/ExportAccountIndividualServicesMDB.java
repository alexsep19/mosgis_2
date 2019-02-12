package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.AccountIndividualService;
import ru.eludia.products.mosgis.db.model.tables.AccountIndividualServiceLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.GisRestStream;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient.Context;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inAccountIndividualServicesQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportAccountIndividualServicesMDB extends GisExportMDB<AccountIndividualServiceLog> {
    
    @EJB
    RestGisFilesClient restGisFilesClient;

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.outExportAccountIndividualServicesQueue")
    Queue outExportAccountIndividualServicesQueue;

    @Override
    protected Get get (UUID uuid) {        
        return ((AccountIndividualServiceLog) ModelHolder.getModel ().get (AccountIndividualServiceLog.class)).getForExport (uuid.toString ());
    }
        
    AckRequest.Ack invoke (DB db, AccountIndividualService.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
            
        switch (action) {
            case PLACING:
            case EDITING:
                return wsGisHouseManagementClient.importAccountIndividualServices (orgPPAGuid, messageGUID, r);
            default: 
                throw new IllegalArgumentException ("No action implemented for " + action.name ());
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("r." + AccountIndividualService.c.ID_CTR_STATUS.lc ()));
        AccountIndividualService.Action action = AccountIndividualService.Action.forStatus (status);        

        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
                                        
        logger.info ("r=" + DB.to.json (r));
       
        sendFile (db, uuid, action, r);
        
    }

    private void sendSoap (DB db, AccountIndividualService.Action action, UUID uuid, Map<String, Object> r) throws SQLException {
        
        try {
            AckRequest.Ack ack = invoke (db, action, uuid, r);
            store (db, ack, r, action.getNextStatus ());
            uuidPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place completed works", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            return;            
        }
        
    }
    
    Queue getQueue (AccountIndividualService.Action action) {
        return outExportAccountIndividualServicesQueue;
    }

    @Override
    protected final Queue getFilesQueue () {
        return null;
    }

    @Override
    protected Col getStatusCol () {
        return AccountIndividualService.c.ID_CTR_STATUS.getCol ();
    }

    @Override
    protected Table getFileLogTable () {
        return null;
    }

    private void sendFile (DB db, UUID uuid, AccountIndividualService.Action action, Map<String, Object> r) throws SQLException {        
        
        final UUID orgppaguid = (UUID) r.get ("orgppaguid");
        
        try {

            try (

                GisRestStream out = new GisRestStream (
                    restGisFilesClient,
                    Context.HOMEMANAGEMENT,
                    orgppaguid, 
                    r.get ("label").toString (), 
                    Long.parseLong (r.get ("len").toString ()),
                    (uploadId, attachmentHash) -> {

                        r.put ("attachmentguid", uploadId);
                        r.put ("attachmenthash", attachmentHash);

                        db.update (AccountIndividualService.class, r);

                        db.update (AccountIndividualServiceLog.class, DB.HASH (
                            "uuid",           r.get ("uuid"),
                            "attachmentguid", uploadId,
                            "attachmenthash", attachmentHash
                        ));
                        
                        sendSoap (db, action, uuid, r);

                    }
                        
                )

            ) {
                db.getStream (ModelHolder.getModel ().get (AccountIndividualServiceLog.class, uuid, "body"), out);
            }
            
        }
        catch (Exception ex) {            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
        }
        
    }

}