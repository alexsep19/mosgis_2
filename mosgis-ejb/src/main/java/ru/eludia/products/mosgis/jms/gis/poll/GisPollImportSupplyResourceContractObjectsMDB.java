package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.InImportSupplyResourceContractObject;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractLog;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractObject;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractObjectLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
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
            .toOne (InImportSupplyResourceContractObject.class, "AS imp", "*").on ("imp.uuid_out_soap = root.uuid")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc () + " AS ppa").on ("imp.uuid_org=org.uuid")
	    .toOne (SupplyResourceContract.class, "AS ctr", "contractrootguid", "uuid").on("ctr.contractrootguid=imp.contractrootguid")
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


	    List<Map<String, Object>> sr_ctr_objects = storeSrContractObjects (db, r, objects);

	    String err_text = "";

	    for (Map<String, Object> sr_ctr_obj: sr_ctr_objects) {

		String fiasHouseGuid = DB.to.String(sr_ctr_obj.get(SupplyResourceContractObject.c.FIASHOUSEGUID.lc()));

		if (DB.ok (sr_ctr_obj.get("err_text"))) {

		    err_text = err_text
			+ "\r\n" + fiasHouseGuid
			+ " " + sr_ctr_obj.get("err_text")
		    ;
		    continue;
		}
            }

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

    public List<Map<String, Object>> storeSrContractObjects(DB db, Map<String, Object> r, List<ExportSupplyResourceContractObjectAddressResultType> objects) throws SQLException,  GisPollRetryException {

	Object uuid_out_soap = r.get("imp.uuid_out_soap");
	Object uuid_sr_ctr = r.get("ctr.uuid");
	Object uuid_user = r.get("imp.uuid_user");
	Object uuid_org = r.get("imp.uuid_org");
	Object uuid_message = r.get("uuid_message");

	List<Map<String, Object>> sr_ctr_objects = new ArrayList<>();

	for (ExportSupplyResourceContractObjectAddressResultType obj : objects) {

	    final Map<String, Object> h = SupplyResourceContractObject.toHASH (obj);

	    if (!DB.ok (h)) continue;

logger.info(DB.to.json(h).toString());

	    h.put ("uuid_org", uuid_org);
	    h.put ("uuid_sr_ctr", uuid_sr_ctr);

	    final Model m = db.getModel();

	    try {
		Object uuid_premise = h.get(SupplyResourceContractObject.c.UUID_PREMISE.lc());

		Select select = m.select(SupplyResourceContractObject.class, EnTable.c.UUID.lc())
		    .where(SupplyResourceContractObject.c.UUID_SR_CTR, uuid_sr_ctr)
		    .and(SupplyResourceContractObject.c.FIASHOUSEGUID, h.get(SupplyResourceContractObject.c.FIASHOUSEGUID.lc())
		);

		if (uuid_premise == null) {
		    select.and(SupplyResourceContractObject.c.UUID_PREMISE.lc() + " IS NULL");
		} else {
		    select.and(SupplyResourceContractObject.c.UUID_PREMISE.lc(), uuid_premise);
		}

		String id = db.getString(select);

		if (DB.ok(id)) { // else keep insert UUID = ObjectGUID
		    h.put (EnTable.c.UUID.lc (), id);
		}

		id = db.upsertId (SupplyResourceContractObject.class, h);

		UUID uuid = DB.to.UUIDFromHex(id);

		h.put(EnTable.c.UUID.lc(), uuid);

		String idLog = db.insertId(SupplyResourceContractObjectLog.class, DB.HASH(
		    "action", VocAction.i.IMPORT_SR_CONTRACT_OBJECTS.getName (),
		    "uuid_object", uuid,
		    "uuid_out_soap", uuid_out_soap,
		    "uuid_user", uuid_user,
		    "uuid_message", uuid_message
                )).toString ();

                db.update (SupplyResourceContractObject.class, DB.HASH (
		    "uuid", uuid,
		    "id_log", idLog
                ));

//		mergeObjectSubjects (db, h, uuid_out_soap, uuid_message, uuid_user);

            } catch (SQLException e) {

                if (e.getErrorCode () != 20000) {
		    throw e;
		}

		String s = new StringTokenizer (e.getMessage (), "\n\r")
		    .nextToken ()
		    .replace ("ORA-20000: ", "");

		h.put("err_text", s);
            }

	    sr_ctr_objects.add(h);
	}

	return sr_ctr_objects;
    }

}
