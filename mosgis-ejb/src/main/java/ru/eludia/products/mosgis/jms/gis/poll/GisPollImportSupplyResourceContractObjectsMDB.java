package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractLog;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisHouseManagementClient;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractObjectAddressResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outImportSupplyResourceContractObjectsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportSupplyResourceContractObjectsMDB extends GisPollMDB {

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Override
    protected Get get (UUID uuid) {

        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")
            .toOne (SupplyResourceContractLog.class, "AS log", "uuid", "action", "uuid_object", "uuid_message", "uuid_user", "uuid_out_soap")
		.on ("log.uuid_out_soap=root.uuid")
	    .toOne(SupplyResourceContract.class, "AS ctr", SupplyResourceContract.c.CONTRACTROOTGUID.lc() + " AS contractrootguid")
		.on("log.uuid_object=ctr.uuid")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc () + " AS ppa").on ("ctr.uuid_org=org.uuid")
        ;
        
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid = (UUID) r.get ("ppa");
                        
        try {            
            
            GetStateResult state = getState (orgPPAGuid, r);

	    ErrorMessageType errorMessage = state.getErrorMessage();

	    if (errorMessage != null) {

		fail (db, uuid, errorMessage.getErrorCode (), errorMessage.getDescription ());

		return;
	    }

            List<GetStateResult.ExportSupplyResourceContractObjectAddress> results = state.getExportSupplyResourceContractObjectAddress();
            
            if (results == null) throw new GisPollException ("0", "Сервис ГИС вернул пустой результат");
	    
	    GetStateResult.ExportSupplyResourceContractObjectAddress result = results.get(0);

	    if (result == null) throw new GisPollException("0", "Сервис ГИС вернул пустой результат");

	    List<ExportSupplyResourceContractObjectAddressResultType> objects = result.getObjectAddress();

	    if (objects == null) throw new GisPollException("0", "Сервис ГИС вернул пустой результат");

	    logger.log(Level.WARNING, "Handling ObjectAddress not implemented yet..");

	    if (!result.isIsLastPage()) {
		logger.log(Level.WARNING, "Is NOT last page, more ObjectAddress exists, pagination not implemented yet..");
	    }

	    db.update(OutSoap.class, HASH(
		"uuid", uuid,
		"id_status", DONE.getId()
	    ));

        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {            
            ex.register (db, uuid, r);
        }
        
    }


    private void fail (DB db, UUID uuid, String code, String text) throws SQLException {
        
        logger.log (Level.WARNING, code + " " + text);

	db.update (OutSoap.class, HASH (
            "uuid", uuid,
            "id_status", DONE.getId (),
            "is_failed", 1,
            "err_code",  code,
            "err_text",  text
        ));
    }

    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisHouseManagementClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
        }
        catch (Fault ex) {
            throw new GisPollException (ex.getFaultInfo ());
        }
        catch (Throwable ex) {            
            throw new GisPollException (ex);
        }
        
        checkIfResponseReady (rp);
        
        return rp;
        
    }
}
