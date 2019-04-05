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
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramDocument;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramFile;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramFileLog;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisCapitalRepairClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportOverhaulRegionalProgramsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportOverhaulRegionalProgramsMDB extends GisExportMDB <OverhaulRegionalProgramLog> {
    
    @EJB
    WsGisCapitalRepairClient wsGisCapitalRepairClient;
    
    @Resource (mappedName = "mosgis.outExportOverhaulRegionalProgramsQueue")
    Queue outExportOverhaulRegionalProgramsQueue;
    
    @Resource (mappedName = "mosgis.inExportOverhaulRegionalProgramFilesQueue")
    Queue inExportOverhaulRegionalProgramFilesQueue;
    
    private List <Map <String, Object>> getFiles (UUID uuid) throws SQLException {
        
        DB db = ModelHolder.getModel ().getDb ();
        return db.getList (db.getModel ()
                .select (OverhaulRegionalProgramFile.class, "*")
                .toOne  (OverhaulRegionalProgramFileLog.class, "AS log", "ts_start_sending", "err_text").on ()
                .toOne  (OverhaulRegionalProgramDocument.class, "AS doc").on ()
                .toOne  (OverhaulRegionalProgram.class, "AS pr").on ("doc.program_uuid = pr.uuid")
                .toOne  (OverhaulRegionalProgramLog.class, "AS pr_log").where ("uuid", uuid).on ("pr.id_log = pr_log.uuid")
                .and    ("is_deleted", 0)
        );
        
    }
    
    private String getStatus (UUID uuid) throws SQLException {
        
        DB db = ModelHolder.getModel ().getDb ();
        return db.getString (db.getModel ()
                .get   (OverhaulRegionalProgramLog.class, uuid, OverhaulRegionalProgram.c.ID_ORP_STATUS.lc ())
        );
        
    }
    
    AckRequest.Ack invoke (DB db, OverhaulRegionalProgram.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
            
        switch (action) {
            case PROJECT_PUBLISH: return wsGisCapitalRepairClient.importRegionalProgramProject (orgPPAGuid, messageGUID, r);
            case PLACING:         return wsGisCapitalRepairClient.importRegionalProgram (orgPPAGuid, messageGUID, r);
            case PROJECT_DELETE:  return wsGisCapitalRepairClient.deleteRegionalProgramProject (orgPPAGuid, messageGUID, r);
            case ANNUL:           return wsGisCapitalRepairClient.annulRegionalProgram (orgPPAGuid, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    @Override
    protected void handleRecord(DB db, UUID uuid, Map<String, Object> r) throws Exception {
        
        VocGisStatus.i status = VocGisStatus.i.forId (getStatus (uuid));
        OverhaulRegionalProgram.Action action = OverhaulRegionalProgram.Action.forStatus (status);
        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
        
        if (isWaiting (getFiles (uuid), db, action.getFailStatus (), r)) return;
        
        r = OverhaulRegionalProgramLog.getForExport (db, uuid.toString ());
        
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
    
    Queue getQueue (OverhaulRegionalProgram.Action action) {        
        return outExportOverhaulRegionalProgramsQueue;
    }

    @Override
    protected final Queue getFilesQueue () {
        return inExportOverhaulRegionalProgramFilesQueue;
    }

    @Override
    protected Table getFileLogTable() {
        return ModelHolder.getModel ().get (OverhaulRegionalProgramFileLog.class);
    }

    @Override
    protected Col getStatusCol() {
        return OverhaulRegionalProgram.c.ID_ORP_STATUS.getCol ();
    }
    
}
