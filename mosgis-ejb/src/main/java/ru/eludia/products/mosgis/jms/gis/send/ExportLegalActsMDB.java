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
import ru.eludia.products.mosgis.db.model.tables.LegalAct;
import ru.eludia.products.mosgis.db.model.tables.LegalActLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.voc.VocLegalActLevel;
import ru.eludia.products.mosgis.ws.rest.clients.tools.GisRestStream;
import ru.eludia.products.mosgis.ws.rest.clients.RestGisFilesClient;
import ru.eludia.products.mosgis.ws.rest.clients.RestGisFilesClient.Context;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisUkClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.uk_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportLegalActsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportLegalActsMDB extends GisExportMDB<LegalActLog> {
    
    @EJB
    RestGisFilesClient restGisFilesClient;

    @EJB
    WsGisUkClient wsGisUkClient;

    @Resource (mappedName = "mosgis.outExportLegalActsQueue")
    Queue outExportLegalActsQueue;

    @Override
    protected Get get (UUID uuid) {        
	final MosGisModel m = ModelHolder.getModel();
	return m.get(LegalActLog.class, uuid, "*");
    }
        
    AckRequest.Ack invoke (DB db, LegalAct.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");

	VocLegalActLevel.i level = VocLegalActLevel.i.forId(r.get(LegalAct.c.LEVEL_.lc()));

        switch (action) {
	    case EDITING:
            case PLACING:
		switch (level) {
		    case MUNICIPAL:
			return wsGisUkClient.importDocumentsMunicipal(orgPPAGuid, messageGUID, r);
		    case REGIONAL:
			return wsGisUkClient.importDocumentsRegion(orgPPAGuid, messageGUID, r);
		}
            case ANNULMENT:
		switch (level) {
		    case MUNICIPAL:
			return wsGisUkClient.deleteDocumentsMunicipal(orgPPAGuid, messageGUID, r);
		    case REGIONAL:
			return wsGisUkClient.deleteDocumentsRegion(orgPPAGuid, messageGUID, r);
		}
            default: 
                throw new IllegalArgumentException ("No action implemented for " + action.name ());
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {

	r = LegalActLog.getForExport(db, uuid.toString());

	VocGisStatus.i status = VocGisStatus.i.forId (r.get ("r." + LegalAct.c.ID_CTR_STATUS.lc ()));
        LegalAct.Action action = LegalAct.Action.forStatus (status);

        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }

        logger.info ("r=" + DB.to.json (r));
        
        switch (action) {
            case ANNULMENT:
                sendSoap (db, action, uuid, r);
                break;
            default:
                sendFileThenSoap (db, uuid, action, r);
                break;
        }
    }

    private void sendSoap (DB db, LegalAct.Action action, UUID uuid, Map<String, Object> r) throws SQLException {
        
        try {
            AckRequest.Ack ack = invoke (db, action, uuid, r);
            store (db, ack, r, action.getNextStatus ());
            uuidPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place legal acts", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            return;            
        }
        
    }
    
    Queue getQueue (LegalAct.Action action) {
        return outExportLegalActsQueue;
    }

    @Override
    protected final Queue getFilesQueue () {
        return null;
    }

    @Override
    protected Col getStatusCol () {
        return LegalAct.c.ID_CTR_STATUS.getCol ();
    }

    @Override
    protected Table getFileLogTable () {
        return null;
    }

    private void sendFileThenSoap (DB db, UUID uuid, LegalAct.Action action, Map<String, Object> r) throws SQLException {
        
        final UUID orgppaguid = (UUID) r.get ("orgppaguid");
        
        try {

            try (

                GisRestStream out = new GisRestStream (
                    restGisFilesClient,
		    Context.CONTENTMANAGEMENT,
                    orgppaguid, 
                    r.get ("label").toString (), 
                    Long.parseLong (r.get ("len").toString ()),
                    (uploadId, attachmentHash) -> {

                        r.put ("attachmentguid", uploadId);
                        r.put ("attachmenthash", attachmentHash);

                        db.update (LegalAct.class, r);

                        db.update (LegalActLog.class, DB.HASH (
                            "uuid",           r.get ("uuid"),
                            "attachmentguid", uploadId,
                            "attachmenthash", attachmentHash
                        ));
                        
                        sendSoap (db, action, uuid, r);

                    }
                        
                )

            ) {
                db.getStream (ModelHolder.getModel ().get (LegalActLog.class, uuid, "body"), out);
            }
            
        }
        catch (Exception ex) {            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
        }
        
    }

}