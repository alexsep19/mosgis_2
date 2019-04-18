package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationCategory;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationCalculationKind;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationCategoryLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.incoming.InCitizenCompensationCategory;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisMSPClient;
import ru.gosuslugi.dom.schema.integration.msp.GetStateResult;
import ru.gosuslugi.dom.schema.integration.msp.ExportCategoryType;
import ru.gosuslugi.dom.schema.integration.msp_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outImportCitizenCompensationCategoriesQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportCitizenCompensationCategoriesMDB extends GisPollMDB {

    @EJB
    WsGisMSPClient wsGisMSPClient;
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")
                                            .toOne (VocOrganization.class, "AS org", "uuid AS uuid_org").on ("org.orgppaguid = root.orgppaguid");
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
                        
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);

	    List<ExportCategoryType> cats = state.getCategory();

            if (cats == null) throw new GisPollException ("0", "Сервис ГИС вернул пустой результат");

	    Map<Object, Map<String, Object>> idxs = storeCitizenCompensationCategories (db, uuid, cats);
            
            for (Object uuid_object: idxs.keySet ()) {
                
                String id_log = db.insertId(CitizenCompensationCategoryLog.class, DB.HASH(
		    "action", VocAction.i.IMPORT_CITIZEN_COMPENSATION_CATEGORIES.getName (),
		    "uuid_object", uuid_object,
		    "uuid_out_soap", uuid
                )).toString ();
                
                db.update (CitizenCompensationCategory.class, DB.HASH (
		    "uuid", uuid_object,
		    "id_log", id_log
                ));
            }

	    db.update(OutSoap.class, HASH(
		"uuid", uuid,
		"id_status", DONE.getId()
	    ));

	    db.update(InCitizenCompensationCategory.class, HASH(
		"uuid", uuid,
		InCitizenCompensationCategory.c.IS_OVER, 1
	    ));
        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {            
            ex.register (db, uuid, r);
        }
        
    }

    public Map<Object, Map<String, Object>> storeCitizenCompensationCategories(DB db, UUID uuid_out_soap, List<ExportCategoryType> cats) throws SQLException {

	List<String> guids = new ArrayList<>();
	List<Map<String, Object>> items = new ArrayList<>();
	List<Map<String, Object>> discounts = new ArrayList<>();

	for (ExportCategoryType cat : cats) {
	    final Map<String, Object> h = CitizenCompensationCategory.toHASH(cat);
	    h.put("uuid_org", h.get("uuid_org"));
	    h.put("id_status", VocAsyncEntityState.i.OK.getId());
	    h.put("id_ctr_status", VocGisStatus.i.APPROVED);
	    h.put("id_ctr_status_gis", h.get("id_ctr_status"));
	    items.add(h);
	    guids.add(h.get(CitizenCompensationCategory.c.CATEGORYGUID.lc()).toString());

	    discounts.addAll((List<Map<String, Object>>)h.get("discounts"));
	}

	db.upsert(CitizenCompensationCategory.class, items, CitizenCompensationCategory.c.CATEGORYGUID.lc());

	db.upsert(
	    CitizenCompensationCalculationKind.class
	    , discounts
	    , CitizenCompensationCalculationKind.c.CATEGORYGUID.lc()
	    , CitizenCompensationCalculationKind.c.SERVICE.lc()
	    , CitizenCompensationCalculationKind.c.CODE_VC_NSI_275.lc()
	    , CitizenCompensationCalculationKind.c.DISCOUNTSIZE.lc()
	);

	return db.getIdx(
	    db.getModel().select(CitizenCompensationCategory.class, "uuid")
		.where(CitizenCompensationCategory.c.CATEGORYGUID.lc() + " IN", guids.toArray())
	);
    }

    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisMSPClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
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
