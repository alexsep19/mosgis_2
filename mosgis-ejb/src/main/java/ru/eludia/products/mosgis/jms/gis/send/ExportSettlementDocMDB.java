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
import ru.eludia.products.mosgis.db.model.tables.SettlementDoc;
import ru.eludia.products.mosgis.db.model.tables.SettlementDocLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocSettlementDocType;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisBillsClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.bills_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inSettlementDocsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportSettlementDocMDB extends GisExportMDB<SettlementDocLog> {
    
    @EJB
    WsGisBillsClient wsGisBillsClient;

    @Resource(mappedName = "mosgis.outExportSettlementDocsQueue")
    Queue outExportSettlementDocsQueue;

    @Override
    protected Get get (UUID uuid) {        
        return ((SettlementDocLog) ModelHolder.getModel ().get (SettlementDocLog.class)).getForExport (uuid.toString ());
    }
        
    AckRequest.Ack invoke (DB db, SettlementDoc.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");

	VocSettlementDocType.i id_type  = VocSettlementDocType.i.forId(r.get("r.id_type"));

	switch (action) {
            case PLACING:
//            case EDITING:

		if (id_type == VocSettlementDocType.i.RSO) {
		    return wsGisBillsClient.importSettlementDocRSO(orgPPAGuid, messageGUID, r);
		}

		if (id_type == VocSettlementDocType.i.UO) {
		    return wsGisBillsClient.importSettlementDocUO(orgPPAGuid, messageGUID, r);
		}

		throw new IllegalArgumentException("Unknown settlement doc type " + id_type);
            default: 
                throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("r." + SettlementDoc.c.ID_SD_STATUS.lc ()));
        SettlementDoc.Action action = SettlementDoc.Action.forStatus (status);        

        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
        
        SettlementDocLog.addItemsForExport (db, r);
                                
        logger.info ("r=" + DB.to.json (r));
       
        try {            
            AckRequest.Ack ack = invoke (db, action, uuid, r);
            store (db, ack, r, action.getNextStatus ());
            uuidPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place settlement doc", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            return;            
        }
        
    }
    
    Queue getQueue (SettlementDoc.Action action) {
        return outExportSettlementDocsQueue;
    }

    @Override
    protected final Queue getFilesQueue () {
        return null;
    }

    @Override
    protected Col getStatusCol () {
        return SettlementDoc.c.ID_SD_STATUS.getCol ();
    }

    @Override
    protected Table getFileLogTable () {
        return null;
    }

}