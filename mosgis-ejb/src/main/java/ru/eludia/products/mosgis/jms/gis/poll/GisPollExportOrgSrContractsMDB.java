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
import ru.eludia.products.mosgis.jms.gis.send.ExportSupplyResourceContractObjectsMDB;
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

    @Resource (mappedName = "mosgis.inExportOrgSrContractObjectsQueue")
    private Queue inExportOrgSrContractObjectsQueue;

    @Resource (mappedName = "mosgis.inExportOrgSrContractsQueue")
    private Queue inExportOrgSrContractsQueue;

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

            if (DB.ok (r.get ("ts_rp"))) {
                Timestamp ts = Timestamp.valueOf (r.get ("ts").toString ());
                Timestamp ts_rp = Timestamp.valueOf (r.get ("ts_rp").toString ());
                if ((ts_rp.getTime () - ts.getTime ()) > 60 * 1000L) throw new GisPollException ("0", "Асинхронный ответ не сформирован более чем за минуту");
            }

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


	    for (ExportSupplyResourceContractResultType i : contracts) {
		try {
		    store (db, r, i);
		}
		catch (UnknownSomethingException ex) {
		    String msg = "ДРСО " + i.getContractRootGUID() + ", " + ex.getMessage ();
		    logger.warning (msg);
		    Map<String, Object> map = db.getMap (db.getModel ().get (OutSoap.class, uuid, "*"));
		    StringBuilder sb = new StringBuilder (DB.to.String (map.get ("err_text")));
		    if (sb.length () > 0) sb.append (";\n");
		    sb.append (msg);
		    db.update (OutSoap.class, HASH (
			"uuid", uuid,
			"is_failed", 1,
			"err_code", "0",
			"err_text", sb.toString ()
		    ));
		}
	    }

	    logger.log(Level.INFO, "import supply resource contracts DONE, scheduling import objects...");

	    uuidPublisher.publish(inExportOrgSrContractObjectsQueue, (UUID) r.get("log.uuid"));


	    if (DB.ok(result.isIsLastPage())) {

		logger.log(Level.INFO, "Last supply resource contracts page, import complete.");

	    } else {

		logger.log(Level.INFO, "NOT Last supply resource contracts page, scheduling next page...");

		String id_log = db.insertId (VocOrganizationLog.class, HASH (
		    "action", VocAction.i.IMPORT_SR_CONTRACTS,
		    "uuid_object", r.get("log.uuid_object"),
		    "uuid_user", r.get("log.uuid_user"),
		    "exportcontractrootguid", result.getExportContractRootGUID()
		)).toString ();

		db.update (VocOrganization.class, HASH (
		    "orgrootentityguid", r.get("log.uuid_object"),
		    "id_log", id_log
		));

		uuidPublisher.publish(inExportOrgSrContractsQueue, id_log);
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

    public void store (DB db, Map<String, Object> r, ExportSupplyResourceContractResultType t) throws SQLException, UnknownSomethingException {

	Object uuid_out_soap = r.get("log.uuid_out_soap");
	Object uuid_org = r.get("log.uuid_object");
	Object uuid_user = r.get("log.uuid_user");
	Object uuid_message = r.get("log.uuid_message");

	final Map<String, Object> h = ExportSupplyResourceContract.toHASH (t);

	if (!DB.ok (h)) return;

	if (!DB.ok(h.get("id_ctr_status_gis"))) {
	    throw new UnknownSomethingException("Неизвестный VersionStatus " + t.getVersionStatus());
	}

	if (DB.eq (h.get("id_ctr_status_gis"), VocGisStatus.i.PROJECT.getId())) {
	    logger.log(Level.INFO, "Skipping supply resource contract " + t.getContractRootGUID() + " due to VersionStatus = Draft approve blocked by gisgkh...");
	    return;
	}

	if(DB.ok(h.get(SupplyResourceContract.c.UUID_ORG.lc()))
	    && !checkOrgGUID(db, h.get(SupplyResourceContract.c.UUID_ORG.lc()))
	) {
	    throw new UnknownSomethingException("Не найдена организация с orgRootEntityGUID " + h.get(SupplyResourceContract.c.UUID_ORG.lc()));
	};

	if(DB.ok(h.get(SupplyResourceContract.c.UUID_ORG_CUSTOMER.lc()))
	    && !checkOrgGUID(db, h.get(SupplyResourceContract.c.UUID_ORG_CUSTOMER.lc()))
	) {
	    throw new UnknownSomethingException("Не найдена организация с orgRootEntityGUID " + h.get(SupplyResourceContract.c.UUID_ORG.lc()));
	};

	h.put (SupplyResourceContract.c.UUID_ORG.lc(), uuid_org);

	try {

	    UUID uuid = DB.to.UUIDFromHex(db.upsertId (SupplyResourceContract.class, h, SupplyResourceContract.c.CONTRACTROOTGUID.lc()));

	    h.put(EnTable.c.UUID.lc(), uuid);

	    String idLog = db.insertId(SupplyResourceContractLog.class, HASH(
		"uuid_vc_org_log", r.get("log.uuid"),
		"action", VocAction.i.IMPORT_SR_CONTRACTS.getName (),
		"uuid_object", uuid,
		"uuid_out_soap", uuid_out_soap,
		"uuid_user", uuid_user,
		"uuid_message", uuid_message
	    )).toString ();

	    db.update (SupplyResourceContract.class, HASH (
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

	    throw new UnknownSomethingException(s);
	}
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

	    if (DB.ok(id)) {
		i.put (EnTable.c.UUID.lc (), id);
		db.update(SupplyResourceContractSubject.class, i);
	    } else { // else keep insert UUID = TransportGUID
		id = db.insertId (SupplyResourceContractSubject.class, i).toString();
	    }

            
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

    private boolean checkOrgGUID(DB db, Object orgRootEntityGUID) throws SQLException, UnknownSomethingException {

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

	String inVocOrganizationUuid = db.getString(db.getModel()
	    .select(InVocOrganization.class, InVocOrganization.c.UUID.lc())
	    .where(InVocOrganization.c.ORGROOTENTITYGUID.lc(), orgRootEntityGUID)
	    .and(InVocOrganization.c.TS, Operator.GT, LocalDate.now().minusDays(5))
	    .orderBy(InVocOrganization.c.TS.lc() + " DESC")
	);

	if (DB.ok(inVocOrganizationUuid)) {

	    Integer outSoapStatus = db.getInteger(OutSoap.class, inVocOrganizationUuid, "id_status");

	    if (outSoapStatus == DONE.getId()) {
		UUID uuid = (UUID) db.insertId (InVocOrganization.class, HASH (
		    InVocOrganization.c.ORGROOTENTITYGUID.lc(), orgRootEntityGUID
		));

		uuidPublisher.publish (inOrgQueue, uuid);
	    }

	}

	throw new UnknownSomethingException("Не найдена организация с orgRootEntityGUID " + orgRootEntityGUID);
    }

    private class UnknownSomethingException extends Exception {

        public UnknownSomethingException (String s) {
            super (s);
        }
    }
}
