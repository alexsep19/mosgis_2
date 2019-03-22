package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.incoming.InLegalAct;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.uk_service_async.Fault;
import ru.eludia.products.mosgis.db.model.tables.LegalAct;
import ru.eludia.products.mosgis.db.model.tables.LegalActLog;
import ru.eludia.products.mosgis.db.model.tables.LegalActOktmo;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisUkClient;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.uk.ExportDocumentType;
import ru.gosuslugi.dom.schema.integration.uk.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outImportLegalActsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportLegalActs extends GisPollMDB {

    @EJB
    WsGisUkClient wsGisUkClient;

    @Resource(mappedName = "mosgis.inImportLegalActFilesQueue")
    Queue inImportLegalActFilesQueue;

    @EJB
    protected UUIDPublisher UUIDPublisher;

    @Override
    protected Get get (UUID uuid) {

	return (Get) ModelHolder.getModel().get(getTable(), uuid, "AS root", "*")
	    .toOne(VocOrganization.class, "AS org", "orgppaguid").on("org.orgppaguid = root.orgppaguid")
	;
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");

        try {
            
            GetStateResult state = getState (orgPPAGuid, r);

	    ErrorMessageType errorMessage = state.getErrorMessage();
	    if (errorMessage != null) {
		throw new GisPollException(errorMessage);
	    }

            List<ExportDocumentType> docs = state.getDocument();
            
            if (docs == null) throw new GisPollException ("0", "Сервис ГИС вернул пустой результат");

	    storeLegalActs(db, uuid, docs);

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

    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisUkClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
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

    public void storeLegalActs(DB db, UUID uuid_out_soap, List<ExportDocumentType> docs) throws SQLException {

	List<Map<String, Object>> items = toHashList(docs);

	List<String> guids = new ArrayList<>();

	for (Map<String, Object> doc : items) {

	    guids.add(doc.get(LegalAct.c.DOCUMENTGUID.lc()).toString());
	}

	db.upsert(LegalAct.class, items, LegalAct.c.DOCUMENTGUID.lc());

	String documentguid = LegalAct.c.DOCUMENTGUID.lc();

	Map<Object, Map<String, Object>> idxs = db.getIdx(
	    ModelHolder.getModel().select(LegalAct.class, "uuid", documentguid)
		.where(documentguid + " IN", guids.toArray())
	);

	Map<String, Object> guid2uuid = DB.HASH();
	for (Object uuid_object : idxs.keySet()) {
	    guid2uuid.put(idxs.get(uuid_object).get(documentguid).toString(), uuid_object);
	}

	List<Map<String, Object>> oktmos = new ArrayList<>();

	for (Map<String, Object> doc : items) {
	    List<Map<String, Object>> o = (List<Map<String, Object>>) doc.get("oktmos");
	    for (Map<String, Object> i : o) {
		i.put("uuid", guid2uuid.get(doc.get(documentguid).toString()));
		oktmos.add(i);
	    }
	}

	LegalActOktmo.store(db, oktmos);

	for (Object uuid_object : idxs.keySet()) {

	    String id_log = db.insertId(LegalActLog.class, DB.HASH(
		"action", VocAction.i.IMPORT_LEGAL_ACTS.getName(),
		"uuid_object", uuid_object,
		"uuid_out_soap", uuid_out_soap
	    )).toString();

	    db.update(LegalAct.class, DB.HASH(
		"uuid", uuid_object,
		"id_log", id_log,
		"uuid_import", uuid_out_soap
	    ));

	    UUIDPublisher.publish(inImportLegalActFilesQueue, (UUID) uuid_object);
	}
    }

    public List<Map<String, Object>> toHashList(List<ExportDocumentType> docs) {

	List<Map<String, Object>> items = new ArrayList<>();

	for (ExportDocumentType doc : docs) {

	    Map<String, Object> h = InLegalAct.toHASH(doc);

	    if (h == null) {
		continue;
	    }

	    items.add(h);
	}

	return items;
    }
}
