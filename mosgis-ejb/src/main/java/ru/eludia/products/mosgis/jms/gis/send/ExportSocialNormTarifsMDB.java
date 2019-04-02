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
import ru.eludia.products.mosgis.db.model.tables.SocialNormTarif;
import ru.eludia.products.mosgis.db.model.tables.SocialNormTarifLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisTariffClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.tariff_service.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportSocialNormTarifsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportSocialNormTarifsMDB extends GisExportMDB<SocialNormTarifLog> {
    
    @EJB
    WsGisTariffClient wsGisTariffClient;

    @Resource (mappedName = "mosgis.outExportSocialNormTarifsQueue")
    Queue outExportSocialNormTarifsQueue;

    @Override
    protected Get get (UUID uuid) {        
	final MosGisModel m = ModelHolder.getModel();
	return m.get(SocialNormTarifLog.class, uuid, "*");
    }
        
    AckRequest.Ack invoke (DB db, SocialNormTarif.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");

        switch (action) {
            case PLACING:
            case EDITING:
		return wsGisTariffClient.importSocialNorms(orgPPAGuid, messageGUID, r);
            case ANNULMENT:
		return wsGisTariffClient.deleteSocialNorms(orgPPAGuid, messageGUID, r);
            default: 
                throw new IllegalArgumentException ("No action implemented for " + action.name ());
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {

	r = SocialNormTarifLog.getForExport(db, uuid.toString());

	VocGisStatus.i status = VocGisStatus.i.forId (r.get ("r." + SocialNormTarif.c.ID_CTR_STATUS.lc ()));
        SocialNormTarif.Action action = SocialNormTarif.Action.forStatus (status);

        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }

        logger.info ("r=" + DB.to.json (r));

	sendSoap(db, action, uuid, r);
    }

    private void sendSoap (DB db, SocialNormTarif.Action action, UUID uuid, Map<String, Object> r) throws SQLException {
        
        try {
            AckRequest.Ack ack = invoke (db, action, uuid, r);
            store (db, ack, r, action.getNextStatus ());
            uuidPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place social norm tarif", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            return;            
        }
        
    }
    
    Queue getQueue (SocialNormTarif.Action action) {
        return outExportSocialNormTarifsQueue;
    }

    @Override
    protected final Queue getFilesQueue () {
        return null;
    }

    @Override
    protected Col getStatusCol () {
        return SocialNormTarif.c.ID_CTR_STATUS.getCol ();
    }

    @Override
    protected Table getFileLogTable () {
        return null;
    }
}