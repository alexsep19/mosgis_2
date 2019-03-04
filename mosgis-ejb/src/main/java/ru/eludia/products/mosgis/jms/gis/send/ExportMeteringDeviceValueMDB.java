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
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceValue;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceValueLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisMeteringClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.device_metering_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportMeteringDeviceValuesQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportMeteringDeviceValueMDB extends GisExportMDB<MeteringDeviceValueLog> {
    
    @EJB
    WsGisMeteringClient wsGisMeteringClient;
    
    @Resource (mappedName = "mosgis.outExportMeteringDeviceValuesQueue")
    Queue outExportHouseMeteringDeviceValuesQueue;
                    
    protected Get get (UUID uuid) {        
        final MosGisModel m = ModelHolder.getModel ();
        return m.get (MeteringDeviceValueLog.class, uuid, "*");
    }
        
    AckRequest.Ack invoke (DB db, MeteringDeviceValue.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
            
        switch (action) {
            case PLACING: return wsGisMeteringClient.importMeteringDeviceValues (orgPPAGuid, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        r = MeteringDeviceValueLog.getForExport (db, uuid.toString ());
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("r." + MeteringDeviceValue.c.ID_CTR_STATUS.lc ()));
        
        MeteringDeviceValue.Action action = MeteringDeviceValue.Action.forStatus (status);        
        
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
    
    Queue getQueue (MeteringDeviceValue.Action action) {        
        return outExportHouseMeteringDeviceValuesQueue;        
    }

    @Override
    protected final Queue getFilesQueue () {
        return null;
    }

    @Override
    protected Table getFileLogTable () {
        return null;
    }

    @Override
    protected Col getStatusCol () {
        return MeteringDeviceValue.c.ID_CTR_STATUS.getCol ();
    }

}