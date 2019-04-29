package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.jms.gis.poll.sr_ctr.ExportSupplyResourceContract;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisHouseManagementClient;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportOrgSrContractsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOrgSrContractsMDB extends GisPollMDB {

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Override
    protected Get get (UUID uuid) {

        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")
            .toOne (VocOrganizationLog.class, "AS log", "uuid", "action", "uuid_object").on ("log.uuid_out_soap=root.uuid")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc () + " AS ppa").on ("log.uuid_object=org.uuid")
        ;
        
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid = (UUID) r.get ("ppa");
                        
        try {            
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            List<GetStateResult.ExportSupplyResourceContractResult> results = state.getExportSupplyResourceContractResult();
            
            if (results == null) throw new GisPollException ("0", "Сервис ГИС вернул пустой результат");
	    
	    GetStateResult.ExportSupplyResourceContractResult result = results.get(0);

	    if (result == null) throw new GisPollException("0", "Сервис ГИС вернул пустой результат");

	    List<ExportSupplyResourceContractResultType> contracts = result.getContract();

	    if (contracts == null) throw new GisPollException("0", "Сервис ГИС вернул пустой результат");

	    List<Map<String, Object>> sr_ctrs = storeSrContracts (db, uuid, r.get("log.uuid_object"), contracts);

	    for (Map<String, Object> i: sr_ctrs) {

		Object uuid_object = i.get(EnTable.c.UUID.lc());

		String id_log = db.insertId(SupplyResourceContractLog.class, DB.HASH(
		    "action", VocAction.i.IMPORT_SR_CONTRACTS.getName (),
		    "uuid_object", uuid_object,
		    "uuid_out_soap", uuid
                )).toString ();
                
                db.update (SupplyResourceContract.class, DB.HASH (
		    "uuid", uuid_object,
		    "id_log", id_log
                ));
            }

	    db.update(OutSoap.class, HASH(
		"uuid", uuid,
		"id_status", DONE.getId()
	    ));
	    if (!result.isIsLastPage()) {
		String exportContractRootGuid = result.getExportContractRootGUID();
		// TODO: 
	    }

        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {            
            ex.register (db, uuid, r);
        }
        
    }

    public List<Map<String, Object>> storeSrContracts(DB db, UUID uuid_out_soap, Object uuid_org, List<ExportSupplyResourceContractResultType> contracts) throws SQLException {

	List<Map<String, Object>> sr_ctrs = new ArrayList<>();
	List<String> guids = new ArrayList<>();

	for (ExportSupplyResourceContractResultType t : contracts) {
	    final Map<String, Object> h = ExportSupplyResourceContract.toHASH (t);
	    if (!DB.ok (h)) continue;
	    h.put ("uuid_org", uuid_org);
	    sr_ctrs.add(h);
	    guids.add(h.get(SupplyResourceContract.c.CONTRACTROOTGUID.lc()).toString());
	}

	db.upsert (SupplyResourceContract.class, sr_ctrs, SupplyResourceContract.c.CONTRACTROOTGUID.lc());

	return db.getList(
	    db.getModel().select(SupplyResourceContract.class, "uuid")
		.where(SupplyResourceContract.c.CONTRACTROOTGUID.lc() + " IN", guids.toArray())
	);
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
