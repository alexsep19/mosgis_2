package ru.eludia.products.mosgis.ws.rest.impl;

import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.TarifDiff;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.TarifDiffFias;
import ru.eludia.products.mosgis.db.model.tables.TarifDiffOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiation;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationOperator;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationValueKindType;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;
import ru.eludia.products.mosgis.rest.api.TarifDiffLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class TarifDiffImpl extends BaseCRUD<TarifDiff> implements TarifDiffLocal {

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

//        select.and (PremiseUsageTarifDiff.c.VALUESTRING.lc () + " LIKE ?%", searchString.toUpperCase ());
        
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
    
    private void checkFilter (JsonObject data, TarifDiff.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

	final Model m = ModelHolder.getModel();

        Select select = m.select (TarifDiff.class, "AS root", "*")
	    .toOne(VocDifferentiation.class, "*").on()
	    .toOne(VocDifferentiationValueKindType.class, "AS type", "id", "label").on()
	    .toMaybeOne(VocDifferentiationOperator.class, "AS op", "id", "label").on()
            .orderBy("type.label")
	    .orderBy("op.label")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

	checkFilter (p.getJsonObject("data"), TarifDiff.c.UUID_TF, select);

	db.addJsonArrayCnt (job, select);

	selectFias(db, job, select);

	selectOktmo(db, job, select);
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

	final Model m = ModelHolder.getModel();

	final JsonObject item = db.getJsonObject (m
            .get (TarifDiff.class, id, "AS root", "*")
            .toOne(VocDifferentiationValueKindType.class, "AS type", "label").on()
	    .toMaybeOne(VocDifferentiationOperator.class, "AS op", "label").on()
        );

        job.add ("item", item);
    });}

    @Override
    public JsonObject getVocs () {return fetchData((db, jb) -> {
        
        VocDifferentiationValueKindType.addTo(jb);
	VocDifferentiationOperator.addTo(jb);

	db.addJsonArrays(jb,
	    db.getModel()
		.select(VocDifferentiation.class
		    , VocDifferentiation.c.DIFFERENTIATIONCODE.lc() + " AS id"
		    , VocDifferentiation.c.DIFFERENTIATIONNAME.lc() + " AS label"
		    , VocDifferentiation.c.ISPLURAL.lc()
		)
		.orderBy(VocDifferentiation.c.DIFFERENTIATIONCODE.lc())
	);
    });}

    @Override
    public JsonObject doCreate(JsonObject p, User user) { return doAction((db, job) -> {

	Map<String, Object> data = getData(p);

	if (!data.containsKey(UUID_ORG)) {
	    data.put(UUID_ORG, user.getUuidOrg());
	}

	Object insertId = db.insertId(TarifDiff.class, data);

	job.add("id", insertId.toString());

	storeFias(db, p, insertId);

	storeOktmo(db, p, insertId);

	logAction(db, user, insertId, VocAction.i.CREATE);

    });}

    @Override
    public JsonObject doUpdate(String id, JsonObject p, User user) {return doAction(db -> {

	JsonObject data = p.getJsonObject("data");

	Map<String, Object> r = getTable().HASH(data, "uuid", id);

	db.update(TarifDiff.class, r);

	storeFias(db, p, id);

	storeOktmo(db, p, id);

	logAction(db, user, id, VocAction.i.UPDATE);
    });}

    private void storeFias(DB db, JsonObject p, Object insertId) throws SQLException {

	JsonObject data = p.getJsonObject("data");

	if (!data.containsKey("fias") || data.isNull("fias")) {
	    return;
	}

	TarifDiffFias.store(db, insertId.toString(),
	     data.getJsonArray("fias").getValuesAs(JsonString.class).stream()
		.map((t) -> {
		    return DB.HASH("id", t.getString());
		})
		.collect(Collectors.toList())
	);
    }

    private void selectFias(DB db, JsonObjectBuilder job, Select select) throws SQLException {

	Map<Object, Map<String, Object>> idx = db.getIdx(select, "uuid");

	final Model m = db.getModel();

	db.addJsonArrays(job,
	    m.select(TarifDiffFias.class, "AS fias", "*")
		.toOne(VocBuildingAddress.class, "AS b", "houseguid", "postalcode", "label").on()
		.where("uuid IN", idx.keySet().toArray())
		.orderBy("b.label")
	);
    }

    private void storeOktmo(DB db, JsonObject p, Object insertId) throws SQLException {

	JsonObject data = p.getJsonObject("data");

	if (!data.containsKey("oktmo") || data.isNull("oktmo")) {
	    return;
	}

	TarifDiffOktmo.store(db, insertId.toString(),
	    data.getJsonArray("oktmo").getValuesAs(JsonString.class).stream()
		.map((t) -> {
		    return DB.HASH("id", t.getString());
		})
		.collect(Collectors.toList())
	);
    }

    private void selectOktmo(DB db, JsonObjectBuilder job, Select select) throws SQLException {

	Map<Object, Map<String, Object>> idx = db.getIdx(select, "uuid");

	final Model m = db.getModel();

	db.addJsonArrays(job,
	    m.select(TarifDiffOktmo.class, "AS oktmo", "*")
		.toOne(VocOktmo.class, "AS o", "id", "code", "site_name").on()
		.where("uuid IN", idx.keySet().toArray())
		.orderBy("o.code")
	);
    }

}