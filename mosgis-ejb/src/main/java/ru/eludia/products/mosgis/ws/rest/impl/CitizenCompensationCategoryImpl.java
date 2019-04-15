package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.annotation.Resource;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationCategory;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.InCitizenCompensationCategory;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationCalculationKind;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationCategoryLegalAct;
import ru.eludia.products.mosgis.db.model.tables.LegalAct;
import ru.eludia.products.mosgis.db.model.voc.VocBudgetLevel;
import ru.eludia.products.mosgis.db.model.voc.VocCitizenCompensationHousing;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocServiceType;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi275;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;
import ru.eludia.products.mosgis.rest.api.CitizenCompensationCategoryLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CitizenCompensationCategoryImpl extends BaseCRUD<CitizenCompensationCategory> implements CitizenCompensationCategoryLocal {

    @Resource(mappedName = "mosgis.inImportCitizenCompensationCategoriesQueue")
    Queue inImportCitizenCompensationCategoriesQueue;

    @Override
    public Queue getQueue (VocAction.i action) {

        switch (action) {
	    case IMPORT_CITIZEN_COMPENSATION_CATEGORIES:
		return inImportCitizenCompensationCategoriesQueue;
            default:
                return null;
        }

    }

    private void filterOffDeleted (Select select) {
        select.and ("is_deleted", 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        filterOffDeleted (select);

        final String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return;

        select.and (CitizenCompensationCategory.c.LABEL_UC.lc () + " LIKE ?%", searchString.toUpperCase ());
        
    }
    
    private void applySearch (final Search search, Select select) {        

        if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else {
            filterOffDeleted (select);
        }

    }
    
    private void checkFilter (JsonObject data, CitizenCompensationCategory.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
	    .toMaybeOne(VocOktmo.class, "site_name AS oktmo_label").on()
	    .toMaybeOne (VocBudgetLevel.class, "AS vc_budget_level", "*").on()
            .orderBy ("root." + CitizenCompensationCategory.c.LABEL_UC.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        JsonObject data = p.getJsonObject ("data");
        
        checkFilter (data, CitizenCompensationCategory.c.UUID_ORG, select);

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "AS root", "*")
	    .toMaybeOne(VocOktmo.class, "site_name AS oktmo_label").on()
	    .toMaybeOne (VocBudgetLevel.class, "AS vc_budget_level", "*").on()
        ));
        
        VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
	VocBudgetLevel.addTo(job);
    });}

    @Override
    public JsonObject getCalculation(String id) {return fetchData ((db, job) -> {

        db.addJsonArrays (job, ModelHolder.getModel ()
            .select (CitizenCompensationCalculationKind.class, "AS root", "*")
	    .toMaybeOne(VocServiceType.class, "*").on()
	    .toMaybeOne(VocCitizenCompensationHousing.class, "*").on()
	    .toMaybeOne(Nsi275.class, "AS vw_nsi_275", "*").on("root.code_vc_nsi_275 = vw_nsi_275.id")
            .where  (CitizenCompensationCalculationKind.c.UUID_CIT_COMP_CAT, id)
        );
    });}

    @Override
    public JsonObject getLegalActs(String id) {return fetchData ((db, job) -> {

        db.addJsonArrays (job, ModelHolder.getModel ()
	    .select(LegalAct.class, "AS root", "*")
            .toOne (CitizenCompensationCategoryLegalAct.class, "AS cla", "*")
		.where("uuid", id)
		.on("root.uuid = cla.uuid_legal_act")
	    .orderBy(LegalAct.c.APPROVEDATE.lc() + " DESC")
        );
    });}

    @Override
    public JsonObject getVocs () {return fetchData ((db, job) -> {
        
        final MosGisModel m = ModelHolder.getModel ();

        VocGisStatus.addLiteTo(job);
        VocAction.addTo (job);
        VocBudgetLevel.addTo(job);
//        Nsi295.i.addTo (job);
    });}
    
    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db, job) -> {
        
        String userOrg = user.getUuidOrg ();
        
        if (userOrg == null) {
            logger.warning ("User has no org set, access prohibited");
            throw new ValidationException ("foo", "Отсутствует организация пользователя, доступ запрещен");
        }

        final Table table = getTable ();

	Map<String, Object> data = getData (p);

	data.put(UUID_ORG, user.getUuidOrg());

	Object insertId = db.insertId (table, data);

	job.add ("id", insertId.toString ());
        
        logAction (db, user, insertId, VocAction.i.CREATE);
        
        db.update (table, HASH (EnTable.c.UUID, insertId,
                CitizenCompensationCategory.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));
        
        logAction (db, user, insertId, VocAction.i.APPROVE);

    });}

    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {

        db.update (getTable (), getData (p,
            EnTable.c.UUID, id,
            CitizenCompensationCategory.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.UPDATE);

        logAction (db, user, id, VocAction.i.APPROVE);

    });}

    @Override
    public JsonObject doDelete(String id, User user) { return doAction((db) -> {

	    db.update(getTable(), HASH (EnTable.c.UUID, id,
		CitizenCompensationCategory.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_DELETE.getId()
	    ));

	    logAction(db, user, id, VocAction.i.DELETE);

    });}

    @Override
    public JsonObject doUndelete(String id, User user) { return doAction((db) -> {

	    db.update(getTable(), HASH (EnTable.c.UUID, id,
		CitizenCompensationCategory.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_UNDELETE.getId()
	    ));

	    logAction(db, user, id, VocAction.i.UNDELETE);

    });}

    @Override
    public JsonObject doImport(User user) {

	try (DB db = ModelHolder.getModel().getDb()) {

	    String uuidOrg = user.getUuidOrg();

	    if (uuidOrg == null) {
		logger.warning("User has no org set, access prohibited");
		throw new ValidationException("foo", "Отсутствует организация пользователя, доступ запрещен");
	    }

	    Map<String, Object> data = new HashMap ();

	    data.put(InCitizenCompensationCategory.c.UUID_ORG.lc(), uuidOrg);

	    UUID uuid = (UUID) db.insertId(InCitizenCompensationCategory.class, data);

	    publishMessage(VocAction.i.IMPORT_CITIZEN_COMPENSATION_CATEGORIES, uuid.toString());

	    return Json.createObjectBuilder().add("id", uuid.toString()).build();

	} catch (Exception ex) {
	    Logger.getLogger(CitizenCompensationCategory.class.getName()).log(Level.SEVERE, null, ex);
	}

	return EMPTY_JSON_OBJECT;
    }
}