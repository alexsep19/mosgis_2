package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.sql.Timestamp;
import javax.jms.Queue;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.InImportSupplyResourceContractObject;
import ru.eludia.products.mosgis.db.model.incoming.InVocOrganization;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractLog;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubjectLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.jms.gis.poll.sr_ctr.ExportSupplyResourceContract;
import ru.eludia.products.mosgis.jms.gis.send.ImportSupplyResourceContractObjectsMDB;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisHouseManagementClient;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
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

    @Resource (mappedName = "mosgis.inOrgQueue")
    private Queue inOrgQueue;

    @Resource (mappedName = "mosgis.inImportSupplyResourceContractObjectsQueue")
    private Queue inImportSupplyResourceContractObjectsQueue;

    @Override
    protected Get get (UUID uuid) {

        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")
            .toOne (VocOrganizationLog.class, "AS log", "uuid", "action", "uuid_object", "uuid_message", "uuid_user", "uuid_out_soap")
		.on ("log.uuid_out_soap=root.uuid")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc () + " AS ppa").on ("log.uuid_object=org.uuid")
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

            List<GetStateResult.ExportSupplyResourceContractResult> results = state.getExportSupplyResourceContractResult();
            
            if (results == null) throw new GisPollException ("0", "Сервис ГИС вернул пустой результат");
	    
	    GetStateResult.ExportSupplyResourceContractResult result = results.get(0);

	    if (result == null) throw new GisPollException("0", "Сервис ГИС вернул пустой результат");

	    List<ExportSupplyResourceContractResultType> contracts = result.getContract();

	    if (contracts == null) throw new GisPollException("0", "Сервис ГИС вернул пустой результат");

	    List<Map<String, Object>> sr_ctrs = storeSrContracts (db, r, contracts);

	    String err_text = "";

	    long ts_from = System.currentTimeMillis();

	    for (Map<String, Object> sr_ctr: sr_ctrs) {

		String contractRootGuid = DB.to.String(sr_ctr.get(SupplyResourceContract.c.CONTRACTROOTGUID.lc()));

		if (DB.ok (sr_ctr.get("err_text"))) {

		    err_text = err_text
			+ "\r\n" + contractRootGuid
			+ " " + sr_ctr.get("err_text")
		    ;
		    continue;
		}

		UUID idImpObj = (UUID) db.insertId(InImportSupplyResourceContractObject.class, DB.HASH(
		    "contractrootguid", sr_ctr.get(SupplyResourceContract.c.CONTRACTROOTGUID.lc()),
		    "uuid_org", r.get("log.uuid_object"),
		    "ts_from", new Timestamp (ts_from)
                ));

		uuidPublisher.publish(inImportSupplyResourceContractObjectsQueue, idImpObj);

		ts_from = ts_from + ImportSupplyResourceContractObjectsMDB.WS_GIS_THROTTLE_MS;
            }

	    if (!result.isIsLastPage()) {
		logger.log(Level.WARNING, "Is NOT last page, more contracts exists, pagination not implemented yet..");
	    }

	    db.update(OutSoap.class, HASH(
		"uuid", uuid,
		"err_text", DB.ok(err_text)? err_text : null,
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

    public List<Map<String, Object>> storeSrContracts(DB db, Map<String, Object> r, List<ExportSupplyResourceContractResultType> contracts) throws SQLException,  GisPollRetryException {

	Object uuid_out_soap = r.get("log.uuid_out_soap");
	Object uuid_org = r.get("log.uuid_object");
	Object uuid_user = r.get("log.uuid_user");
	Object uuid_message = r.get("log.uuid_message");

	List<Map<String, Object>> sr_ctrs = new ArrayList<>();

	for (ExportSupplyResourceContractResultType t : contracts) {
	    final Map<String, Object> h = ExportSupplyResourceContract.toHASH (t);

	    if (!DB.ok (h)) continue;

	    Map<String, Object> sr_ctr = DB.HASH(
		SupplyResourceContract.c.CONTRACTROOTGUID.lc(), h.get(SupplyResourceContract.c.CONTRACTROOTGUID.lc())
	    );

	    if(DB.ok(h.get(SupplyResourceContract.c.UUID_ORG.lc()))
		&& !checkOrgGUID(db, h.get(SupplyResourceContract.c.UUID_ORG.lc()))
	    ) {
		sr_ctr.put("err_text", "Не найдена организация с orgRootEntityGUID " + h.get(SupplyResourceContract.c.UUID_ORG.lc()));
		continue;
	    };

	    if(DB.ok(h.get(SupplyResourceContract.c.UUID_ORG_CUSTOMER.lc()))
		&& !checkOrgGUID(db, h.get(SupplyResourceContract.c.UUID_ORG_CUSTOMER.lc()))
	    ) {
		sr_ctr.put("err_text", "Не найдена организация с orgRootEntityGUID " + h.get(SupplyResourceContract.c.UUID_ORG.lc()));
		continue;
	    };

	    h.put (SupplyResourceContract.c.ID_CTR_STATUS.lc(), VocGisStatus.i.PENDING_RQ_RELOAD.getId());
	    h.put (SupplyResourceContract.c.UUID_ORG.lc(), uuid_org);

	    try {
                
		UUID uuid = DB.to.UUIDFromHex(db.upsertId (SupplyResourceContract.class, h, SupplyResourceContract.c.CONTRACTROOTGUID.lc()));

		h.put(EnTable.c.UUID.lc(), uuid);
		sr_ctr.put(EnTable.c.UUID.lc(), uuid);

		String idLog = db.insertId(SupplyResourceContractLog.class, DB.HASH(
		    "action", VocAction.i.IMPORT_SR_CONTRACTS.getName (),
		    "uuid_object", uuid,
		    "uuid_out_soap", uuid_out_soap,
		    "uuid_user", uuid_user,
		    "uuid_message", uuid_message
                )).toString ();

                db.update (SupplyResourceContract.class, DB.HASH (
		    "uuid", uuid,
		    "id_log", idLog
                ));


		mergeSubjects (db, h, uuid_out_soap, uuid_message, uuid_user);

            } catch (SQLException e) {

                if (e.getErrorCode () != 20000) {
		    throw e;
		}

		String s = new StringTokenizer (e.getMessage (), "\n\r")
		    .nextToken ()
		    .replace ("ORA-20000: ", "");

		sr_ctr.put("err_text", s);

		logger.log(Level.INFO, sr_ctr.get("contractrootguid") + " " + s);
            }

	    db.update (SupplyResourceContract.class, DB.HASH (
		"contractrootguid", h.get("contractrootguid"),
		"id_ctr_status", DB.ok(sr_ctr.get("err_text"))?
		    VocGisStatus.i.FAILED_RELOAD.getId() : VocGisStatus.i.APPROVED.getId()
	    ), "contractrootguid");

	    sr_ctrs.add(sr_ctr);
	}

	return sr_ctrs;
    }

    void mergeSubjects (DB db, Map<String, Object> h, Object uuid_out_soap, Object uuid_message, Object uuid_user) throws SQLException {

	Object uuid = h.get(EnTable.c.UUID.lc());

	db.d0 (new QP("UPDATE " + SupplyResourceContractSubject.TABLE_NAME + " SET is_deleted = 1 WHERE uuid_sr_ctr_obj IS NULL AND uuid_sr_ctr = ?", uuid));

	final Model m = db.getModel();

	for (Map<String, Object> i: (List<Map<String, Object>>) h.get (SupplyResourceContractSubject.TABLE_NAME)) {
            
	    i.put (EnTable.c.IS_DELETED.lc(), 0);
            i.put (SupplyResourceContractSubject.c.UUID_SR_CTR.lc(), uuid);
	    i.put (SupplyResourceContractSubject.c.UUID_SR_CTR_OBJ.lc(), null);

	    String id = db.getString (m.select(SupplyResourceContractSubject.class, EnTable.c.UUID.lc())
		.where(SupplyResourceContractSubject.c.UUID_SR_CTR, uuid)
		.and(SupplyResourceContractSubject.c.UUID_SR_CTR_OBJ.lc() + " IS NULL")
		.and(SupplyResourceContractSubject.c.CODE_VC_NSI_3, i.get(SupplyResourceContractSubject.c.CODE_VC_NSI_3.lc()))
		.and(SupplyResourceContractSubject.c.CODE_VC_NSI_239, i.get(SupplyResourceContractSubject.c.CODE_VC_NSI_239.lc()))
		.and(SupplyResourceContractSubject.c.STARTSUPPLYDATE, i.get(SupplyResourceContractSubject.c.STARTSUPPLYDATE.lc()))
	    );

	    if (DB.ok(id)) { // else keep insert UUID = TransportGUID
		i.put (EnTable.c.UUID.lc (), id);
	    }

	    id = db.upsertId (SupplyResourceContractSubject.class, i);
            
            String idLog = db.insertId (SupplyResourceContractSubjectLog.class, HASH (
		"action", VocAction.i.IMPORT_SR_CONTRACTS,
		"uuid_object", id,
		"uuid_user", uuid_user,
		"uuid_out_soap", uuid_out_soap,
		"uuid_message", uuid_message
	    )).toString ();

	    db.update (SupplyResourceContractSubject.class, HASH (
		"uuid", id,
		"id_log", idLog
	    ));
        }
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

    private boolean checkOrgGUID(DB db, Object orgRootEntityGUID) throws SQLException, GisPollRetryException {

	if (orgRootEntityGUID == null) {
	    return true;
	}

	String uuidOrg = db.getString(db.getModel()
	    .select(VocOrganization.class, "uuid")
	    .where(EnTable.c.UUID, orgRootEntityGUID)
	    .and("is_deleted", 0)
	);

        if (DB.ok(uuidOrg)) {
            return true;
        }

	logger.log(Level.SEVERE, "Не найдена организация с orgRootEntityGUID " + orgRootEntityGUID);

	String inVocOrganizationUuid = db.getString(db.getModel()
	    .select(InVocOrganization.class, InVocOrganization.c.UUID.lc())
	    .where(InVocOrganization.c.ORGROOTENTITYGUID.lc(), orgRootEntityGUID)
	    .and(InVocOrganization.c.TS, Operator.GT, LocalDate.now().minusDays(5))
	    .orderBy(InVocOrganization.c.TS.lc() + " DESC")
	);

	if (DB.ok(inVocOrganizationUuid)) {

	    Integer outSoapStatus = db.getInteger(OutSoap.class, inVocOrganizationUuid, "id_status");

	    if (outSoapStatus != DONE.getId()) {
		return false;
	    }

	    UUID uuid = (UUID) db.insertId (InVocOrganization.class, HASH (
		InVocOrganization.c.ORGROOTENTITYGUID.lc(), orgRootEntityGUID
	    ));

	    uuidPublisher.publish (inOrgQueue, uuid);

	    return false;
	}

	return false;
    }
    
}
