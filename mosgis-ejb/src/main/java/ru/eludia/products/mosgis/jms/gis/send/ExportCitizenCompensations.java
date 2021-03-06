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
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensation;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisMSPClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.msp_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportCitizenCompensationsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportCitizenCompensations extends GisExportMDB<CitizenCompensationLog> {
    
    @EJB
    WsGisMSPClient wsGisMSPClient;

    @Resource (mappedName = "mosgis.outExportCitizenCompensationsQueue")
    Queue outExportCitizenCompensationsQueue;

    @Override
    protected Get get (UUID uuid) {        
        return ((CitizenCompensationLog) ModelHolder.getModel ().get (CitizenCompensationLog.class)).getForExport (uuid.toString ());
    }
        
    AckRequest.Ack invoke (DB db, CitizenCompensation.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
            
        switch (action) {
            case PLACING:
            case EDITING:
                return wsGisMSPClient.importCitizenCompensation(orgPPAGuid, messageGUID, r);
            case ANNULMENT:
                return wsGisMSPClient.deleteCitizenCompensation(orgPPAGuid, messageGUID, r);
            default: 
                throw new IllegalArgumentException ("No action implemented for " + action.name ());
        }
    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("r." + CitizenCompensation.c.ID_CTR_STATUS.lc ()));
        CitizenCompensation.Action action = CitizenCompensation.Action.forStatus (status);

        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
        
        CitizenCompensationLog.addItemsForExport (db, r);
                                
        logger.info ("r=" + DB.to.json (r));
       
        try {            
            AckRequest.Ack ack = invoke (db, action, uuid, r);
            store (db, ack, r, action.getNextStatus ());
            uuidPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place completed works", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            return;            
        }
        
    }
    
    Queue getQueue (CitizenCompensation.Action action) {
        return outExportCitizenCompensationsQueue;
    }

    @Override
    protected final Queue getFilesQueue () {
        return null;
    }

    @Override
    protected Col getStatusCol () {
        return CitizenCompensation.c.ID_CTR_STATUS.getCol ();
    }

    @Override
    protected Table getFileLogTable () {
        return null;
    }

}