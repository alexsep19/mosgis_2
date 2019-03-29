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
import ru.eludia.products.mosgis.db.model.tables.PremiseUsageTarif;
import ru.eludia.products.mosgis.db.model.tables.PremiseUsageTarifLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.ws.rest.clients.tools.GisRestStream;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisTariffClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.tariff_service.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportPremiseUsageTarifsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportPremiseUsageTarifsMDB extends GisExportMDB<PremiseUsageTarifLog> {
    
    @EJB
    WsGisTariffClient wsGisTariffClient;

    @Resource (mappedName = "mosgis.outExportPremiseUsageTarifsQueue")
    Queue outExportPremiseUsageTarifsQueue;

    @Override
    protected Get get (UUID uuid) {        
	final MosGisModel m = ModelHolder.getModel();
	return m.get(PremiseUsageTarifLog.class, uuid, "*");
    }
        
    AckRequest.Ack invoke (DB db, PremiseUsageTarif.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");

        switch (action) {
            case PLACING:
            case EDITING:
		return wsGisTariffClient.importResidentialPremisesUsage(orgPPAGuid, messageGUID, r);
            case ANNULMENT:
//		return wsGisTariffClient.deleteResidentialPremisesUsage(orgPPAGuid, messageGUID, r);
            default: 
                throw new IllegalArgumentException ("No action implemented for " + action.name ());
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {

	r = PremiseUsageTarifLog.getForExport(db, uuid.toString());

	VocGisStatus.i status = VocGisStatus.i.forId (r.get ("r." + PremiseUsageTarif.c.ID_CTR_STATUS.lc ()));
        PremiseUsageTarif.Action action = PremiseUsageTarif.Action.forStatus (status);

        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }

        logger.info ("r=" + DB.to.json (r));

	sendSoap(db, action, uuid, r);
    }

    private void sendSoap (DB db, PremiseUsageTarif.Action action, UUID uuid, Map<String, Object> r) throws SQLException {
        
        try {
            AckRequest.Ack ack = invoke (db, action, uuid, r);
            store (db, ack, r, action.getNextStatus ());
            uuidPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place premise usage tarif", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            return;            
        }
        
    }
    
    Queue getQueue (PremiseUsageTarif.Action action) {
        return outExportPremiseUsageTarifsQueue;
    }

    @Override
    protected final Queue getFilesQueue () {
        return null;
    }

    @Override
    protected Col getStatusCol () {
        return PremiseUsageTarif.c.ID_CTR_STATUS.getCol ();
    }

    @Override
    protected Table getFileLogTable () {
        return null;
    }
}