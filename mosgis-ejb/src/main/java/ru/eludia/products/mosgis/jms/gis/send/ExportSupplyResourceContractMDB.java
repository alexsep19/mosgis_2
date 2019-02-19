package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractFileLog;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inHouseSupplyResourceContractsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportSupplyResourceContractMDB extends GisExportMDB<SupplyResourceContractLog> {
    
    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @Resource (mappedName = "mosgis.outExportHouseSupplyResourceContractsQueue")
    Queue outExportHouseSupplyResourceContractsQueue;
    
    @Resource (mappedName = "mosgis.inHouseSupplyResourceContractFilesQueue")
    Queue inHouseSupplyResourceContractFilesQueue;
                
    protected Get get (UUID uuid) {        
        return ((SupplyResourceContractLog) ModelHolder.getModel ().get (SupplyResourceContractLog.class)).getForExport (uuid);
    }
        
    AckRequest.Ack invoke (DB db, SupplyResourceContract.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");
            
        switch (action) {
            case PLACING:     
            case EDITING:     
                return wsGisHouseManagementClient.importSupplyResourceContract (orgPPAGuid, messageGUID, r);
            case ANNULMENT:
                return wsGisHouseManagementClient.annulSupplyResourceContract  (orgPPAGuid, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("ctr." + SupplyResourceContract.c.ID_CTR_STATUS.lc ()));
        SupplyResourceContract.Action action = SupplyResourceContract.Action.forStatus (status);

        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
        
        if (action != SupplyResourceContract.Action.ANNULMENT) {
            
            SupplyResourceContractLog.addFilesForExport (db, r);
            List<Map<String, Object>> files = (List<Map<String, Object>>) r.get ("files");
            if (isWaiting (files, db, action.getFailStatus (), r)) return;        

            SupplyResourceContractLog.addRefsForExport (db, r);
            
        }
                        
        logger.info ("r=" + DB.to.json (r));
       
        try {            
            AckRequest.Ack ack = invoke (db, action, uuid, r);
            store (db, ack, r, action.getNextStatus ());
            uuidPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place supply resource contract", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            return;            
        }
        
    }
    
    Queue getQueue (SupplyResourceContract.Action action) {
        return outExportHouseSupplyResourceContractsQueue;
    }

    @Override
    protected final Queue getFilesQueue () {
        return inHouseSupplyResourceContractFilesQueue;
    }

    @Override
    protected Table getFileLogTable () {
        return ModelHolder.getModel ().get (SupplyResourceContractFileLog.class);
    }

    @Override
    protected Col getStatusCol () {
        return SupplyResourceContract.c.ID_CTR_STATUS.getCol ();
    }

}