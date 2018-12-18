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
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContract;
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContractFileLog;
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContractLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inHousePublicPropertyContractsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportPublicPropertyContractMDB extends GisExportMDB<PublicPropertyContractLog> {
    
    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @Resource (mappedName = "mosgis.outExportHousePublicPropertyContractsQueue")
    Queue outExportHousePublicPropertyContractsQueue;
    
    @Resource (mappedName = "mosgis.inHousePublicPropertyContractFilesQueue")
    Queue inHousePublicPropertyContractFilesQueue;
                
    protected Get get (UUID uuid) {        
        return ((PublicPropertyContractLog) ModelHolder.getModel ().get (PublicPropertyContractLog.class)).getForExport (uuid);
    }
        
    AckRequest.Ack invoke (DB db, PublicPropertyContract.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");
            
        switch (action) {
            case PLACING:     return wsGisHouseManagementClient.importPublicPropertyContract (orgPPAGuid, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("ctr." + PublicPropertyContract.c.ID_CTR_STATUS.lc ()));
        PublicPropertyContract.Action action = PublicPropertyContract.Action.forStatus (status);        
        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }

        PublicPropertyContractLog.addFilesForExport (db, r);
        List<Map<String, Object>> files = (List<Map<String, Object>>) r.get ("files");
        if (isWaiting (files, db, action.getFailStatus (), r)) return;        
        
        PublicPropertyContractLog.addRefsForExport (db, r);

        Model m = db.getModel ();                
                        
        logger.info ("r=" + DB.to.json (r));
       
        try {            
            AckRequest.Ack ack = invoke (db, action, uuid, r);
            store (db, ack, r, action.getNextStatus ());
            uuidPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place voting protocol", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            return;            
        }
        
    }
    
    Queue getQueue (PublicPropertyContract.Action action) {        
        return outExportHousePublicPropertyContractsQueue;        
    }

    @Override
    protected final Queue getFilesQueue () {
        return inHousePublicPropertyContractFilesQueue;
    }

    @Override
    protected Table getFileLogTable () {
        return ModelHolder.getModel ().get (PublicPropertyContractFileLog.class);
    }

    @Override
    protected Col getStatusCol () {
        return PublicPropertyContract.c.ID_CTR_STATUS.getCol ();
    }

}