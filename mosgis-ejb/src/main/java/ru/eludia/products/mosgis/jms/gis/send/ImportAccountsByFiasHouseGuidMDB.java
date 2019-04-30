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
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.HouseLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisHouseManagementClient;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportAccountsByFiasHouseGuidQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ImportAccountsByFiasHouseGuidMDB extends GisExportMDB<HouseLog> {
    
    @EJB
    private WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.outExportAccountsByFiasHouseGuidQueue")
    Queue outExportAccountsByFiasHouseGuidQueue;

    protected Get get (UUID uuid) {        
        
        final MosGisModel m = ModelHolder.getModel ();
        
        return (Get) m
            .get   (HouseLog.class, uuid, "AS root")
            .toOne (House.class, "AS r", House.c.FIASHOUSEGUID.lc () + " AS fiashouseguid").on ()
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc () + " AS orgppaguid").on ("root.uuid_org=org.uuid")
            ;
        
    }
        
    AckRequest.Ack invoke (DB db, UUID messageGUID, Map<String, Object> r) throws Fault, SQLException {                        
        return wsGisHouseManagementClient.exportAccountData ((UUID) r.get ("orgppaguid"), messageGUID, (UUID) r.get ("fiashouseguid"));
    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
                                                        
        logger.info ("r=" + DB.to.json (r));
       
        try {            
            
            AckRequest.Ack ack = invoke (db, uuid, r);
            
            OutSoap.registerAck (db, ack);
            
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
        return outExportAccountsByFiasHouseGuidQueue;
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