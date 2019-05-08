package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.InImportSupplyResourceContractObject;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportSupplyResourceContractObjectsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportSupplyResourceContractObjectsMDB extends UUIDMDB<InImportSupplyResourceContractObject> {
    
    private static final Logger logger = Logger.getLogger (ExportSupplyResourceContractObjectsMDB.class.getName ());

    @EJB
    protected UUIDPublisher uuidPublisher;

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.outExportSupplyResourceContractObjectsQueue")
    Queue q;
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ()
            .get (InImportSupplyResourceContractObject.class, uuid, "AS root", "*")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc () + " AS ppa").on ("root.uuid_org=org.uuid")
	;
    }    

    public static final int WS_GIS_THROTTLE_MS = 10000;

    private boolean checkRetry (Map <String, Object> r) throws SQLException {

	if (!DB.ok (r.get("ts_from"))) {
	    return false;
	}

	Timestamp ts_throttle = new Timestamp(DB.to.timestamp(r.get("ts_from")).getTime() + WS_GIS_THROTTLE_MS);

	Timestamp now = new Timestamp(System.currentTimeMillis());

	if (now.before(ts_throttle)) {
	    logger.log(Level.INFO, "ts_from in future: retry import resource contract objects");
	    uuidPublisher.publish (getOwnQueue (), getUuid ());
	    return true;
	}

	return false;
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map r) throws SQLException {

	if (checkRetry (r)) {
	    return;
	}

        try {

	    AckRequest.Ack ack = wsGisHouseManagementClient.exportSupplyResourceContractObjectAddressData ((UUID) r.get ("ppa"), uuid, (UUID) r.get("contractrootguid"));

            db.update (OutSoap.class, DB.HASH (
                "uuid",     uuid,
                "uuid_ack", ack.getMessageGUID()
            ));
            
            db.update (getTable (), DB.HASH (
                "uuid",          uuid,
                "uuid_out_soap", uuid
            ));
                
            uuidPublisher.publish (q, uuid);
            
        }
        catch (Exception ex) {

	    if (ex.getMessage().contains("HTTP status code 429")) {


		r.remove(EnTable.c.UUID.lc());
		r.put("ts_from", new Timestamp(System.currentTimeMillis() + WS_GIS_THROTTLE_MS));

		UUID idImpObj = (UUID) db.insertId(InImportSupplyResourceContractObject.class, r);

		logger.log(Level.INFO, "HTTP status code 429: retry import resource contract objects " + idImpObj);

		uuidPublisher.publish(getOwnQueue(), idImpObj);

		return;
	    }

	    logger.log (Level.SEVERE, "Cannot import supply resource contract objects", ex);
        }

    }

}