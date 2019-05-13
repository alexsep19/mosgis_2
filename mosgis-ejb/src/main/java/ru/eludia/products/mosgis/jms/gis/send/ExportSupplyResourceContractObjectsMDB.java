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
import javax.xml.ws.WebServiceException;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportSupplyResourceContractObjectsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportSupplyResourceContractObjectsMDB extends GisExportMDB<SupplyResourceContractLog> {
    
    private static final Logger logger = Logger.getLogger (ExportSupplyResourceContractObjectsMDB.class.getName ());

    @EJB
    protected UUIDPublisher uuidPublisher;

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.outExportSupplyResourceContractObjectsQueue")
    Queue outExportSupplyResourceContractObjectsQueue;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ()
            .get (SupplyResourceContractLog.class, uuid, "AS root", "*")
            .toOne (VocOrganizationLog.class, "AS log").on ("root.uuid_vc_org_log=log.uuid")
	    .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc() + " AS ppa").on("log.uuid_object=org.uuid")
	;
    }
        
    AckRequest.Ack invoke (DB db, UUID messageGUID, Map<String, Object> r) throws Fault, SQLException {                        
        return wsGisHouseManagementClient.exportSupplyResourceContractObjectAddressData ((UUID) r.get ("ppa"), messageGUID, (UUID) r.get("contractrootguid"));
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map r) throws SQLException {

        try {

            AckRequest.Ack ack = invoke (db, uuid, r);
            
            OutSoap.registerAck (db, ack);

            db.update (getTable (), DB.HASH (
                "uuid",          uuid,
                "uuid_out_soap", uuid,
		"uuid_message",  ack.getMessageGUID ()
            ));
                
            uuidPublisher.publish (getQueue (), uuid);
            
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place " + getTableClass ().getSimpleName (), ex);
            fail (db, ex.getFaultInfo (), r, VocGisStatus.i.FAILED_STATE);
            return;
        }            
        catch (Exception ex) {
            
            if (ex instanceof WebServiceException && ex.getMessage ().endsWith ("Too Many Requests")) {
                
                logger.log (Level.WARNING, "Let's wait for 5 s...", ex);
                
                try {
                    Thread.sleep (5000L);
                }
                catch (InterruptedException ex1) {
                    logger.log (Level.WARNING, "Interrupted? OK.");
                }
                
                uuidPublisher.publish (ownDestination, uuid);
                
                return;
                
            }
            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);
            fail (db, ex.getMessage (), VocGisStatus.i.FAILED_STATE, ex, r);
            return;            
        }

    }

    Queue getQueue () {
        return outExportSupplyResourceContractObjectsQueue;
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