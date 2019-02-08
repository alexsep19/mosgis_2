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
import ru.eludia.products.mosgis.db.model.tables.Infrastructure;
import ru.eludia.products.mosgis.db.model.tables.InfrastructureLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisInfrastructureClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.infrastructure_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inInfrastructuresQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportInfrastructureMDB extends GisExportMDB<InfrastructureLog> {
    
    @EJB
    WsGisInfrastructureClient wsGisInfrastructureClient;

    @Resource (mappedName = "mosgis.outExportInfrastructuresQueue")
    Queue outExportInfrastructuresQueue;

    @Override
    protected Get get (UUID uuid) {        
        return ((InfrastructureLog) ModelHolder.getModel ().get (InfrastructureLog.class)).getForExport (uuid.toString ());
    }
        
    AckRequest.Ack invoke (DB db, Infrastructure.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");
            
        switch (action) {
            case PLACING:     return wsGisInfrastructureClient.importOKI (orgPPAGuid, messageGUID, r);
//            case CANCEL:      return wsGisServicesClient.cancelInfrastructure (orgPPAGuid, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("r." + Infrastructure.c.ID_IS_STATUS.lc ()));
        Infrastructure.Action action = Infrastructure.Action.forStatus (status);        

        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
        
        InfrastructureLog.addServicesForImport (db, r);
        InfrastructureLog.addPropertiesForImport (db, r);
                                
        logger.info ("r=" + DB.to.json (r));
       
        try {            
            AckRequest.Ack ack = invoke (db, action, uuid, r);
            store (db, ack, r, action.getNextStatus ());
            uuidPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place infrastructure object", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            return;            
        }
        
    }
    
    Queue getQueue (Infrastructure.Action action) {       
        return outExportInfrastructuresQueue;
    }

    @Override
    protected final Queue getFilesQueue () {
        return null;
    }

    @Override
    protected Col getStatusCol () {
        return Infrastructure.c.ID_IS_STATUS.getCol ();
    }

    @Override
    protected Table getFileLogTable () {
        return null;
    }

}