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
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramDocument;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramFile;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramFileLog;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisCapitalRepairClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportOverhaulAddressProgramsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportOverhaulAddressProgramsMDB extends GisExportMDB <OverhaulAddressProgramLog> {
    
    @EJB
    WsGisCapitalRepairClient wsGisCapitalRepairClient;
    
    @Resource (mappedName = "mosgis.outExportOverhaulAddressProgramsQueue")
    Queue outExportOverhaulAddressProgramsQueue;
    
    @Resource (mappedName = "mosgis.inExportOverhaulAddressProgramFilesQueue")
    Queue inExportOverhaulAddressProgramFilesQueue;
    
    private List <Map <String, Object>> getFiles (DB db, UUID uuid) throws SQLException {

        return db.getList (db.getModel ()
                .select (OverhaulAddressProgramFile.class, "*")
                .toOne  (OverhaulAddressProgramFileLog.class, "AS log", "ts_start_sending", "err_text").on ()
                .toOne  (OverhaulAddressProgramDocument.class, "AS doc").on ()
                .toOne  (OverhaulAddressProgram.class, "AS pr").on ("doc.program_uuid = pr.uuid")
                .toOne  (OverhaulAddressProgramLog.class, "AS pr_log").where ("uuid", uuid).on ("pr.id_log = pr_log.uuid")
                .and    ("is_deleted", 0)
        );
        
    }
    
    private String getStatus (DB db, UUID uuid) throws SQLException {

        return db.getString (db.getModel ()
                .get   (OverhaulAddressProgramLog.class, uuid, OverhaulAddressProgram.c.ID_OAP_STATUS.lc ())
        );
        
    }
    
    AckRequest.Ack invoke (DB db, OverhaulAddressProgram.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
            
        switch (action) {
            case PROJECT_PUBLISH: return wsGisCapitalRepairClient.importAddressProgramProject (orgPPAGuid, messageGUID, r);
            case PLACING:         return wsGisCapitalRepairClient.importAddressProgram (orgPPAGuid, messageGUID, r);
            case PROJECT_DELETE:  return wsGisCapitalRepairClient.deleteAddressProgramProject (orgPPAGuid, messageGUID, r);
            case ANNUL:           return wsGisCapitalRepairClient.annulAddressProgram (orgPPAGuid, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    @Override
    protected void handleRecord(DB db, UUID uuid, Map<String, Object> r) throws Exception {
        
        VocGisStatus.i status = VocGisStatus.i.forId (getStatus (db, uuid));
        OverhaulAddressProgram.Action action = OverhaulAddressProgram.Action.forStatus (status);
        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
        
        if (isWaiting (getFiles (db, uuid), db, action.getFailStatus (), r)) return;
        
        r = OverhaulAddressProgramLog.getForExport (db, uuid.toString ());
        
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
    
    Queue getQueue (OverhaulAddressProgram.Action action) {
        return outExportOverhaulAddressProgramsQueue;
    }

    @Override
    protected final Queue getFilesQueue () {
        return inExportOverhaulAddressProgramFilesQueue;
    }

    @Override
    protected Table getFileLogTable() {
        return ModelHolder.getModel ().get (OverhaulAddressProgramFileLog.class);
    }

    @Override
    protected Col getStatusCol() {
        return OverhaulAddressProgram.c.ID_OAP_STATUS.getCol ();
    }
    
}
