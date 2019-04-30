package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import javax.jms.Queue;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.InVocOrganization;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlSupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractLog;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState.i.FAIL;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.jms.gis.poll.sr_ctr.ExportSupplyResourceContract;
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

    @Override
    protected Get get (UUID uuid) {

        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")
            .toOne (VocOrganizationLog.class, "AS log", "uuid", "action", "uuid_object", "uuid_message").on ("log.uuid_out_soap=root.uuid")
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

	    List<Map<String, Object>> sr_ctrs = storeSrContracts (db, uuid, r.get("log.uuid_object"), contracts);

	    String err_text = "";

	    for (Map<String, Object> sr_ctr: sr_ctrs) {

		if (DB.ok (sr_ctr.get("err_text"))) {

		    err_text = err_text
			+ "\r\n" + DB.to.String(sr_ctr.get(SupplyResourceContract.c.CONTRACTROOTGUID.lc()))
			+ " " + sr_ctr.get("err_text")
		    ;
		}

		Object uuid_object = sr_ctr.get("uuid");

		if (!DB.ok (uuid_object)) {
		    continue;
		}

		String id_log = db.insertId(SupplyResourceContractLog.class, DB.HASH(
		    "action", VocAction.i.IMPORT_SR_CONTRACTS.getName (),
		    "uuid_object", uuid_object,
		    "uuid_out_soap", uuid,
		    "uuid_message", r.get("log.uuid_message")
                )).toString ();
                
                db.update (SupplyResourceContract.class, DB.HASH (
		    "uuid", uuid_object,
		    "id_log", id_log
                ));
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

    public List<Map<String, Object>> storeSrContracts(DB db, UUID uuid_out_soap, Object uuid_org, List<ExportSupplyResourceContractResultType> contracts) throws SQLException,  GisPollRetryException {

	List<Map<String, Object>> sr_ctrs = new ArrayList<>();

	for (ExportSupplyResourceContractResultType t : contracts) {
	    final Map<String, Object> h = ExportSupplyResourceContract.toHASH (t);

	    if (!DB.ok (h)) continue;

	    if(DB.ok(h.get(SupplyResourceContract.c.UUID_ORG.lc()))
		&& !checkOrgGUID(db, h.get(SupplyResourceContract.c.UUID_ORG.lc()))
	    ) {
		continue;
	    };

	    if(DB.ok(h.get(SupplyResourceContract.c.UUID_ORG_CUSTOMER.lc()))
		&& !checkOrgGUID(db, h.get(SupplyResourceContract.c.UUID_ORG_CUSTOMER.lc()))
	    ) {
		continue;
	    };

	    h.put ("uuid_org", uuid_org);

	    Map<String, Object> sr_ctr = DB.HASH(
		SupplyResourceContract.c.CONTRACTROOTGUID.lc(), h.get(SupplyResourceContract.c.CONTRACTROOTGUID.lc())
	    );

	    try {
                
		String uuid = db.upsertId (SupplyResourceContract.class, h, SupplyResourceContract.c.CONTRACTROOTGUID.lc());

		sr_ctr.put("uuid", uuid);

		addSubjects (db, uuid_out_soap, uuid, h);

            }
            catch (SQLException e) {

                String s = e.getMessage ();

                if (e.getErrorCode () == 20000) s =
                    new StringTokenizer (e.getMessage (), "\n\r")
                    .nextToken ()
                    .replace ("ORA-20000: ", "");

		sr_ctr.put("err_text", s);
                
            }

	    sr_ctrs.add(sr_ctr);
	}

	return sr_ctrs;
    }

    void addSubjects (DB db, UUID uuid_out_soap, String uuid, Map<String, Object> r) throws SQLException {

	List<Object> ids = new ArrayList ();

	for (Map<String, Object> i: (List<Map<String, Object>>) r.get (SupplyResourceContractSubject.TABLE_NAME)) {
            
            i.put ("uuid_sr_ctr", uuid);
            
            final String u = db.upsertId (SupplyResourceContractSubject.class, i
		, SupplyResourceContractSubject.c.UUID_SR_CTR.lc()
		, SupplyResourceContractSubject.c.CODE_VC_NSI_3.lc()
		, SupplyResourceContractSubject.c.CODE_VC_NSI_239.lc()
		, SupplyResourceContractSubject.c.STARTSUPPLYDATE.lc()
		, SupplyResourceContractSubject.c.ENDSUPPLYDATE.lc()
	    );
            
            i.put (EnTable.c.UUID.lc (), u);
            
            createIdLog (db, SupplyResourceContractSubject.class, uuid_out_soap, u, VocAction.i.IMPORT_SR_CONTRACTS);
	    
	    ids.add (u);
        }

//	final Model m = db.getModel();
//
//	db.delete(m.select(SupplyResourceContractSubject.class, "uuid")
//	    .where("uuid_sr_ctr", uuid)
//	    .and("uuid NOT IN", ids.toArray())
//	);
    }

    private String createIdLog (DB db, Class c, UUID uuid_out_soap, Object id, VocAction.i action) throws SQLException {
	final MosGisModel model = ModelHolder.getModel ();
        return createIdLog (db, model.get (c), uuid_out_soap, id, action);
    }

    private String createIdLog (DB db, Table table, UUID uuid_out_soap, Object id, VocAction.i action) throws SQLException {

	final MosGisModel model = ModelHolder.getModel ();

        Table logTable = model.getLogTable (table);

        if (logTable == null) return null;
        
        String idLog = db.insertId (logTable, HASH (
            "action", action,
            "uuid_object", id,
            "uuid_out_soap", uuid_out_soap
        )).toString ();
        
        db.update (table, HASH (
            table.getPk ().get (0).getName (), id,
            "id_log", idLog
        ));
                
        return idLog;
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
