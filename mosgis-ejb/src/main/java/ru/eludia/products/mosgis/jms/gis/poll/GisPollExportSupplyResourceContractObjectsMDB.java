package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import javax.annotation.Resource;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.LivingRoom;
import ru.eludia.products.mosgis.db.model.tables.NonResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.ResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractLog;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractObject;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractObjectLog;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubjectLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisHouseManagementClient;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractObjectAddressResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportSupplyResourceContractObjectsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportSupplyResourceContractObjectsMDB extends GisPollMDB {

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.inExportOrgSrContractObjectsQueue")
    Queue inExportOrgSrContractObjectsQueue;

    @Override
    protected Get get (UUID uuid) {

        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")
	    .toOne (SupplyResourceContractLog.class, "AS log", "uuid", "action", "uuid_object", "uuid_user", "uuid_out_soap", "uuid_vc_org_log").on ("log.uuid_out_soap=root.uuid")
	    .toOne (SupplyResourceContract.class, "AS ctr", "contractrootguid", "uuid", "id_ctr_status").on("ctr.uuid=log.uuid_object")
	    .toOne (VocOrganizationLog.class, "AS logorg").on ("log.uuid_vc_org_log=logorg.uuid")
	    .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc() + " AS ppa").on("logorg.uuid_object=org.uuid")
        ;
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid = (UUID) r.get ("ppa");

	boolean checkNext = true;

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

            List<GetStateResult.ExportSupplyResourceContractObjectAddress> results = state.getExportSupplyResourceContractObjectAddress();
            
            if (results == null) throw new GisPollException ("0", "Сервис ГИС вернул пустой результат");
	    
	    GetStateResult.ExportSupplyResourceContractObjectAddress result = results.get(0);

	    if (result == null) throw new GisPollException("0", "Сервис ГИС вернул пустой результат");

	    List<ExportSupplyResourceContractObjectAddressResultType> objects = result.getObjectAddress();

	    if (objects == null) throw new GisPollException("0", "Сервис ГИС вернул пустой результат");



	    for (ExportSupplyResourceContractObjectAddressResultType i : objects) {
		try {
		    store (db, r, i);
		}
		catch (UnknownSomethingException ex) {
		    String msg = "ДРСО " + i.getContractRootGUID() + " ОЖФ " + i.getObjectGUID() + ", " + ex.getMessage ();
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
		    continue;
		}
	    }


	    if (!DB.ok(result.isIsLastPage())) {
		logger.log(Level.WARNING, "Is NOT last page, more ObjectAddress exists, pagination not implemented yet..");
	    }

	    db.update(OutSoap.class, HASH(
		"uuid", uuid,
		"id_status", DONE.getId()
	    ));

        }
        catch (GisPollRetryException ex) {
	    checkNext = false;
            return;
        }
        catch (GisPollException ex) {            
            ex.register (db, uuid, r);
        }
        finally {
            if (checkNext) uuidPublisher.publish (inExportOrgSrContractObjectsQueue, (UUID) r.get ("log.uuid_vc_org_log"));
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

    public void store(DB db, Map<String, Object> r, ExportSupplyResourceContractObjectAddressResultType t) throws SQLException, UnknownSomethingException {

	Object uuid_out_soap = r.get("log.uuid_out_soap");
	Object uuid_sr_ctr = r.get("log.uuid_object");
	Object uuid_user = r.get("log.uuid_user");
	Object uuid_message = r.get("uuid_ack");

	Map<String, Object> h = SupplyResourceContractObject.toHASH(t);

	if (!DB.ok (h)) return;

logger.info(DB.to.json(h).toString());

	h.put (SupplyResourceContractObject.c.UUID_SR_CTR.lc(), uuid_sr_ctr);
	h.put (SupplyResourceContractObject.c.ID_CTR_STATUS.lc(), VocGisStatus.i.PENDING_RQ_RELOAD.getId());
	h.put (EnTable.c.IS_DELETED.lc(), 0);

	final Model m = db.getModel();

	try {
	    
	    h.put (SupplyResourceContractObject.c.UUID_PREMISE.lc(), getUuidPremiseOrRoom(db
		, t.getFIASHouseGuid()
		, t.getHouseType()
		, t.getApartmentNumber()
		, t.getRoomNumber()
	    ));

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

	    if (DB.ok(id)) {
		h.put (EnTable.c.UUID.lc (), id);
		db.update(SupplyResourceContractObject.class, h);
	    } else { // else keep insert UUID = ObjectGUID
		id = db.insertId (SupplyResourceContractObject.class, h).toString();
	    }


	    Object uuid = id;

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

	    mergeObjectServices (db, h, uuid_out_soap, uuid_message, uuid_user);

	} catch (SQLException e) {

	    if (e.getErrorCode () != 20000) {
		throw e;
	    }

	    String s = new StringTokenizer (e.getMessage (), "\n\r")
		.nextToken ()
		.replace ("ORA-20000: ", "");

	    throw new UnknownSomethingException(s);
	}

	db.update (SupplyResourceContractObject.class, DB.HASH (
	    "objectguid", h.get("objectguid"),
	    "id_ctr_status", DB.ok(h.get("err_text"))
		? VocGisStatus.i.FAILED_RELOAD.getId() : VocGisStatus.i.APPROVED.getId()
	), "objectguid");
    }

    private UUID getUuidPremiseOrRoom (DB db, Object fiashouseguid, String housetype, String apartmentnumber, String roomnumber) throws SQLException, UnknownSomethingException {

	final UUID uuid_house = getUuidHouse(db, fiashouseguid, housetype);

	if (DB.ok (housetype) && !DB.ok(uuid_house)) {
	    throw new UnknownSomethingException("Не найден дом " + housetype + " " + fiashouseguid);
	}

	final UUID uuid_premise = getUuidPremise(db, uuid_house, apartmentnumber);

	if (DB.ok (roomnumber) && DB.ok(uuid_premise)) {
	    return getUuidRoom(db, uuid_house, uuid_premise, roomnumber);
	}

	return uuid_premise;
    }

    private UUID getUuidHouse (DB db, Object fiashouseguid, String housetype) throws SQLException, UnknownSomethingException {

	if (housetype == null) {
	    return null;
	}

	if (fiashouseguid == null) {
	    return null;
	}

	Integer is_condo = null;
	Integer hasblocks = null;

	switch (housetype) {
	    case "ZHDBlockZastroyki":
		is_condo = 0;
		hasblocks = 1;
		break;
	    case "ZHD":
		is_condo = 0;
		break;
	    case "MKD":
		is_condo = 1;
		break;
	    default:
		throw new UnknownSomethingException ("Unkown house type " + housetype);
	}

	String uuid_house = db.getString(db.getModel()
	    .select(House.class, "uuid", "is_condo")
	    .where(House.c.FIASHOUSEGUID, fiashouseguid)
	    .and(House.c.IS_CONDO, is_condo)
	    .and(House.c.HASBLOCKS, hasblocks)
	);

	if (uuid_house != null) {

	    return DB.to.UUIDFromHex(uuid_house);
	}

	Map<String, Object> house = DB.HASH(
	    "fiashouseguid", fiashouseguid,
	    "is_condo", is_condo,
	    "address", ""
	);

	return DB.to.UUIDFromHex(db.upsertId(House.class, house, "fiashouseguid"));
    }

    private UUID getUuidPremise (DB db, UUID uuid_house, String apartmentnumber) throws SQLException {

	if (!DB.ok (apartmentnumber) || !DB.ok(uuid_house)) {
	    return null;
	}

	String uuid_premise = db.getString (db.getModel().select(ResidentialPremise.class, "uuid")
	    .where("uuid_house", uuid_house)
	    .and ("premisesnum", apartmentnumber)
	);
	
	if (uuid_premise == null) {

	    uuid_premise = db.getString (db.getModel().select(NonResidentialPremise.class, "uuid")
		.where("uuid_house", uuid_house)
		.and ("premisesnum", apartmentnumber)
	    );
	}

	Map <String, Object> p = DB.HASH(
	    "uuid_house", uuid_house,
	    "premisesnum", apartmentnumber
	);

	uuid_premise = db.upsertId (ResidentialPremise.class, p
	    , "uuid_house"
	    , "premisesnum"
	);

	return DB.to.UUIDFromHex(uuid_premise);
    }

    private UUID getUuidRoom (DB db, UUID uuid_house, UUID uuid_premise, String roomnumber) throws SQLException {

	String uuid_room = db.upsertId (LivingRoom.class, DB.HASH(
		"uuid_house", uuid_house,
		"uuid_premise", uuid_premise,
		"roomnumber", roomnumber
	    )
	    , "uuid_house"
	    , "uuid_premise"
	    , "roomnumber"
	);

	return DB.to.UUIDFromHex(uuid_room);
    }

    void mergeObjectServices (DB db, Map<String, Object> h, Object uuid_out_soap, Object uuid_message, Object uuid_user) throws SQLException {

	List<Map<String, Object>> services = (List<Map<String, Object>>) h.get(SupplyResourceContractSubject.TABLE_NAME);

	if (services == null || services.isEmpty()) {
	    return;
	}

	Object uuid = h.get(EnTable.c.UUID.lc());
	Object uuid_sr_ctr = h.get(SupplyResourceContractObject.c.UUID_SR_CTR.lc());

	db.d0 (new QP("UPDATE " + SupplyResourceContractSubject.TABLE_NAME + " SET is_deleted = 1 WHERE uuid_sr_ctr_obj IS NOT NULL AND uuid_sr_ctr = ?", uuid));

	final Model m = db.getModel();

	for (Map<String, Object> i: services) {
            
	    i.put (EnTable.c.IS_DELETED.lc(), 0);
            i.put (SupplyResourceContractSubject.c.UUID_SR_CTR.lc(), uuid_sr_ctr);
	    i.put (SupplyResourceContractSubject.c.UUID_SR_CTR_OBJ.lc(), uuid);

	    String id = db.getString (m.select(SupplyResourceContractSubject.class, EnTable.c.UUID.lc())
		.where(SupplyResourceContractSubject.c.UUID_SR_CTR, uuid)
		.and(SupplyResourceContractSubject.c.UUID_SR_CTR_OBJ.lc() + " IS NOT NULL")
		.and(SupplyResourceContractSubject.c.CODE_VC_NSI_3, i.get(SupplyResourceContractSubject.c.CODE_VC_NSI_3.lc()))
		.and(SupplyResourceContractSubject.c.CODE_VC_NSI_239, i.get(SupplyResourceContractSubject.c.CODE_VC_NSI_239.lc()))
		.and(SupplyResourceContractSubject.c.STARTSUPPLYDATE, i.get(SupplyResourceContractSubject.c.STARTSUPPLYDATE.lc()))
	    );

	    if (DB.ok(id)) { // else keep insert UUID = TransportGUID
		i.put (EnTable.c.UUID.lc (), id);
	    }

	    id = db.upsertId (SupplyResourceContractSubject.class, i);
            
            String idLog = db.insertId (SupplyResourceContractSubjectLog.class, HASH (
		"action", VocAction.i.IMPORT_SR_CONTRACT_OBJECTS,
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

    private class UnknownSomethingException extends Exception {

        public UnknownSomethingException (String s) {
            super (s);
        }
    }
}
