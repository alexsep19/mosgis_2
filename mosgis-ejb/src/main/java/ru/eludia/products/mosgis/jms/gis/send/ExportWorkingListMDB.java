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
import ru.eludia.products.mosgis.db.model.tables.WorkingList;
import ru.eludia.products.mosgis.db.model.tables.WorkingListItem;
import ru.eludia.products.mosgis.db.model.tables.WorkingListLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisServicesClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.services_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inWorkingListsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportWorkingListMDB extends GisExportMDB<WorkingListLog> {
    
    @EJB
    WsGisServicesClient wsGisServicesClient;
    
    @Resource (mappedName = "mosgis.outExportWorkingListsQueue")
    Queue outExportHouseWorkingListsQueue;
    
    @Resource (mappedName = "mosgis.outImportWorkingListsQueue")
    Queue outImportHouseWorkingListsQueue;
                    
    @Override
    protected Get get (UUID uuid) {        
        return ((WorkingListLog) ModelHolder.getModel ().get (WorkingListLog.class)).getForExport (uuid.toString ());
    }
        
    AckRequest.Ack invoke (DB db, WorkingList.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid_1");
        if (orgPPAGuid == null) orgPPAGuid = (UUID) r.get ("orgppaguid_2");
            
        switch (action) {
            case PLACING:     return wsGisServicesClient.importWorkingList (orgPPAGuid, messageGUID, r);
            case REFRESHING:  return wsGisServicesClient.exportWorkingList (orgPPAGuid, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("r." + WorkingList.c.ID_CTR_STATUS.lc ()));
        WorkingList.Action action = WorkingList.Action.forStatus (status);        

        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
        
        WorkingListItem.addTo (db, r);
                                
        logger.info ("r=" + DB.to.json (r));
       
        try {            
            AckRequest.Ack ack = invoke (db, action, uuid, r);
            store (db, ack, r, action.getNextStatus ());
            uuidPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place working list", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            return;            
        }
        
    }
    
    Queue getQueue (WorkingList.Action action) {
        
        switch (action) {
            case REFRESHING:
                return outImportHouseWorkingListsQueue;
            default:
                return outExportHouseWorkingListsQueue;
        }
        
    }

    @Override
    protected final Queue getFilesQueue () {
        return null;
    }

    @Override
    protected Col getStatusCol () {
        return WorkingList.c.ID_CTR_STATUS.getCol ();
    }

    @Override
    protected Table getFileLogTable () {
        return null;
    }

}