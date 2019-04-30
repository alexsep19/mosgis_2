package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.Collections;
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
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractLog;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inImportSupplyResourceContractObjectsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ImportSupplyResourceContractObjectsMDB extends UUIDMDB<SupplyResourceContractLog> {
    
    private static final Logger logger = Logger.getLogger (ImportSupplyResourceContractObjectsMDB.class.getName ());

    @EJB
    protected UUIDPublisher UUIDPublisher;

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.outImportSupplyResourceContractObjectsQueue")
    Queue q;
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ()
            .get (SupplyResourceContractLog.class, uuid, "AS log", "*")
	    .toOne(SupplyResourceContract.class, "AS ctr", SupplyResourceContract.c.CONTRACTROOTGUID.lc() + " AS contractrootguid")
		.on("log.uuid_object=ctr.uuid")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc () + " AS ppa").on ("ctr.uuid_org=org.uuid")
	;
    }    
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map r) throws SQLException {
                
        try {

	    AckRequest.Ack ack = wsGisHouseManagementClient.exportSupplyResourceContractObjectAddressData ((UUID) r.get ("ppa"), uuid, (UUID) r.get("contractrootguid"));

            db.update (OutSoap.class, DB.HASH (
                "uuid",     uuid,
                "uuid_ack", ack.getMessageGUID()
            ));
            
            db.update (getTable (), DB.HASH (
                "uuid",          uuid,
                "uuid_out_soap", uuid,
		"uuid_message",  ack.getMessageGUID()
            ));
                
            UUIDPublisher.publish (q, uuid);
            
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot import supply resource contract objects", ex);
        }

    }

}