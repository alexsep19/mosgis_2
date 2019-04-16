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
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramDocument;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramFile;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramFileLog;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisCapitalRepairClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportOverhaulShortProgramsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportOverhaulShortProgramsMDB extends GisExportMDB <OverhaulShortProgramLog> {
    
    @EJB
    WsGisCapitalRepairClient wsGisCapitalRepairClient;
    
    @Resource (mappedName = "mosgis.outExportOverhaulShortProgramsQueue")
    Queue outExportOverhaulShortProgramsQueue;
    
    @Resource (mappedName = "mosgis.inExportOverhaulShortProgramFilesQueue")
    Queue inExportOverhaulShortProgramFilesQueue;
    
    private List <Map <String, Object>> getFiles (DB db, UUID uuid) throws SQLException {

        return db.getList (db.getModel ()
                .select (OverhaulShortProgramFile.class, "*")
                .toOne  (OverhaulShortProgramFileLog.class, "AS log", "ts_start_sending", "err_text").on ()
                .toOne  (OverhaulShortProgramDocument.class, "AS doc").on ()
                .toOne  (OverhaulShortProgram.class, "AS pr").on ("doc.program_uuid = pr.uuid")
                .toOne  (OverhaulShortProgramLog.class, "AS pr_log").where ("uuid", uuid).on ("pr.id_log = pr_log.uuid")
                .and    ("is_deleted", 0)
        );
        
    }
    
    private String getStatus (DB db, UUID uuid) throws SQLException {

        return db.getString (db.getModel ()
                .get   (OverhaulShortProgramLog.class, uuid, OverhaulShortProgram.c.ID_OSP_STATUS.lc ())
        );
        
    }
    
    AckRequest.Ack invoke (DB db, OverhaulShortProgram.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
            
        switch (action) {
            case PROJECT_PUBLISH: return wsGisCapitalRepairClient.importShortProgramProject (orgPPAGuid, messageGUID, r);
            case PLACING:         return wsGisCapitalRepairClient.importShortProgram (orgPPAGuid, messageGUID, r);
            case PROJECT_DELETE:  return wsGisCapitalRepairClient.deleteShortProgramProject (orgPPAGuid, messageGUID, r);
            case ANNUL:           return wsGisCapitalRepairClient.annulShortProgram (orgPPAGuid, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    @Override
    protected void handleRecord(DB db, UUID uuid, Map<String, Object> r) throws Exception {
        
        VocGisStatus.i status = VocGisStatus.i.forId (getStatus (db, uuid));
        OverhaulShortProgram.Action action = OverhaulShortProgram.Action.forStatus (status);
        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
        
        if (isWaiting (getFiles (db, uuid), db, action.getFailStatus (), r)) return;
        
        r = OverhaulShortProgramLog.getForExport (db, uuid.toString ());
        
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
    
    Queue getQueue (OverhaulShortProgram.Action action) {
        return outExportOverhaulShortProgramsQueue;
    }

    @Override
    protected final Queue getFilesQueue () {
        return inExportOverhaulShortProgramFilesQueue;
    }

    @Override
    protected Table getFileLogTable() {
        return ModelHolder.getModel ().get (OverhaulShortProgramFileLog.class);
    }

    @Override
    protected Col getStatusCol() {
        return OverhaulShortProgram.c.ID_OSP_STATUS.getCol ();
    }
    
}
