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
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisHouseManagementClient;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportHouseDataByFiasHouseGuidQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ImportHouseByFiasHouseGuidMDB extends GisExportMDB<VocBuildingLog> {
    
    @EJB
    private WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @Resource (mappedName = "mosgis.outExportHouseDataByFiasHouseGuidQueue")
    Queue outExportHouseDataByFiasHouseGuidQueue;
                    
    protected Get get (UUID uuid) {        
        final MosGisModel m = ModelHolder.getModel ();
        return m.get (VocBuildingLog.class, uuid, "*");
    }
        
    AckRequest.Ack invoke (DB db, UUID messageGUID, Map<String, Object> r) throws Fault, SQLException {                        
        return wsGisHouseManagementClient.exportHouseData (null, messageGUID, (UUID) r.get ("uuid_object"));
    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
                                                        
        logger.info ("r=" + DB.to.json (r));
       
        try {            
            
            AckRequest.Ack ack = invoke (db, uuid, r);
            
            db.update (getTable (), DB.HASH (
                "uuid",          r.get ("uuid"),
                "uuid_out_soap", r.get ("uuid"),
                "uuid_message",  ack.getMessageGUID ()
            ));
            
            uuidPublisher.publish (getQueue (), ack.getRequesterMessageGUID ());
            
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place " + getTableClass ().getSimpleName (), ex);
            fail (db, ex.getFaultInfo (), r, VocGisStatus.i.FAILED_STATE);
            return;
        }
        catch (Exception ex) {            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, ex.getMessage (), VocGisStatus.i.FAILED_STATE, ex, r);
            return;            
        }
        
    }
    
    Queue getQueue () {        
        return outExportHouseDataByFiasHouseGuidQueue;        
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
        return null;
    }

}