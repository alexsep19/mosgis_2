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
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkTypeLog;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisNsiClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportOverhaulWorkTypesQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportOverhaulWorkTypesMDB extends GisExportMDB <VocOverhaulWorkTypeLog> {

    @EJB
    WsGisNsiClient wsGisNsiClient;
    
    @Resource (mappedName = "mosgis.outExportOverhaulWorkTypesQueue")
    Queue outExportOverhaulWorkTypesQueue;
    
    @Override
    protected Get get (UUID uuid) {
        return ((VocOverhaulWorkTypeLog) ModelHolder.getModel ().get (VocOverhaulWorkTypeLog.class)).getForExport (uuid.toString ());
    }
    
    Queue getQueue (VocOverhaulWorkType.Action action) {       
        return outExportOverhaulWorkTypesQueue;
    }
    
    AckRequest.Ack invoke (DB db, VocOverhaulWorkType.Action action, UUID messageGUID,  Map <String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");
            
        switch (action) {
            case PLACING:     return wsGisNsiClient.importCapitalRepairWork(orgPPAGuid, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get (VocOverhaulWorkType.c.ID_OWT_STATUS.lc ()));
        VocOverhaulWorkType.Action action = VocOverhaulWorkType.Action.forStatus (status);
        
        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
        
        logger.info ("r=" + DB.to.json (r));
        
        try {            
            AckRequest.Ack ack = invoke (db, action, uuid, r);
            store (db, ack, r, action.getNextStatus ());
            uuidPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place overhaul work type", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            db.update (VocOverhaulWorkType.class, HASH (
                    "uuid", uuid,
                    "id_status", VocAsyncEntityState.i.FAIL.getId ()
            ));
            return;            
        }
        
    }

    @Override
    protected Queue getFilesQueue() {
        return null;
    }

    @Override
    protected Table getFileLogTable() {
        return null;
    }

    @Override
    protected Col getStatusCol() {
        return VocOverhaulWorkType.c.ID_OWT_STATUS.getCol ();
    }
    
}
