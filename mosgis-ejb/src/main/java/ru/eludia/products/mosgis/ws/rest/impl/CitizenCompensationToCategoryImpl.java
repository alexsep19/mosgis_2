package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.Map;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationCalculationKind;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationCategory;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationToCategory;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationToCategoryService;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.CitizenCompensationToCategoryLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CitizenCompensationToCategoryImpl extends BaseCRUD<CitizenCompensationToCategory> implements CitizenCompensationToCategoryLocal {

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

        select.and ("label_uc LIKE ?%", searchString.toUpperCase ());
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

    private void checkFilter (JsonObject data, CitizenCompensationToCategory.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {

	final Model m = db.getModel();

	Select select = m.select (getTable (), "AS root", "*")
	    .toOne(CitizenCompensationCategory.class, "AS ct", "label").on()
            .orderBy (CitizenCompensationToCategory.c.PERIODFROM.lc() + " DESC")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        JsonObject data = p.getJsonObject ("data");

        checkFilter (data, CitizenCompensationToCategory.c.UUID_CIT_COMP, select);

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);


	Map<Object, Map<String, Object>> idx = db.getIdx(select, "uuid");

	db.addJsonArrayCnt(job
	    , m.select(CitizenCompensationToCategoryService.class, "uuid", "id_service")
	    .where("uuid IN", idx.keySet().toArray())
	);
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        final JsonObject item = db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
        );

        job.add ("item", item);

    });}

    @Override
    public JsonObject getVocs () {return fetchData((db, jb) -> {

        VocGisStatus.addLiteTo(jb);
        VocAction.addTo (jb);
	CitizenCompensationCategory.addTo (db, jb);
	CitizenCompensationCalculationKind.addServicesTo(db, jb);
    });}

    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db, job) -> {

        final Table table = getTable ();

        Map<String, Object> d = getData (p);

        if (table.getColumn (UUID_ORG) != null && !d.containsKey (UUID_ORG)) d.put (UUID_ORG, user.getUuidOrg ());

        Object insertId = db.insertId (table, d);


	JsonObject data = p.getJsonObject ("data");

	if (data.containsKey ("vc_service_types")) {
	    CitizenCompensationToCategoryService.store(db, insertId, data.getJsonArray("vc_service_types"));
	}


        job.add ("id", insertId.toString ());
        
        logAction (db, user, insertId, VocAction.i.CREATE);

    });}

    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        db.update (getTable (), getData (p,
            "uuid", id
        ));


	JsonObject data = p.getJsonObject ("data");

	if (data.containsKey ("vc_service_types")) {
	    CitizenCompensationToCategoryService.store(db, id, data.getJsonArray("vc_service_types"));
	}


        logAction (db, user, id, VocAction.i.UPDATE);
                        
    });}
}