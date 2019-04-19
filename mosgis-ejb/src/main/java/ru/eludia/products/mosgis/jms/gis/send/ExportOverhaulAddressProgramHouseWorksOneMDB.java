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
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramHouseWork;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramHouseWorkLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisCapitalRepairClient;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportOverhaulAddressProgramHouseWorksOneQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportOverhaulAddressProgramHouseWorksOneMDB extends GisExportMDB <OverhaulAddressProgramHouseWorkLog> {
    
    @EJB
    WsGisCapitalRepairClient wsGisCapitalRepairClient;
    
    @Resource (mappedName = "mosgis.outExportOverhaulAddressProgramHouseWorksOneQueue")
    Queue outExportOverhaulAddressProgramHouseWorksOneQueue;
    
    AckRequest.Ack invoke (DB db, OverhaulAddressProgramHouseWork.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
            
        switch (action) {
            case ANNUL:           return wsGisCapitalRepairClient.annulAddressProgramWork (orgPPAGuid, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    @Override
    protected void handleRecord(DB db, UUID uuid, Map<String, Object> r) throws Exception {
        
        r = OverhaulAddressProgramHouseWorkLog.getForExport (db, uuid.toString ());
        logger.info ("r=" + DB.to.json (r));
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("id_oaphw_status"));
        OverhaulAddressProgramHouseWork.Action action = OverhaulAddressProgramHouseWork.Action.forStatus (status);
        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
       
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
    
    Queue getQueue (OverhaulAddressProgramHouseWork.Action action) {        
        return outExportOverhaulAddressProgramHouseWorksOneQueue;
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
        return OverhaulAddressProgramHouseWork.c.ID_OAPHW_STATUS.getCol ();
    }
    
}
