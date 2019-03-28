package ru.eludia.products.mosgis.ws.rest.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
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
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.TarifDiffFias;
import ru.eludia.products.mosgis.db.model.tables.TarifDiffNsi;
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

	selectEnumeration(db, job, select);
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

	storeEnumeration(db, p, insertId);

	logAction(db, user, insertId, VocAction.i.CREATE);

    });}

    @Override
    public JsonObject doUpdate(String id, JsonObject p, User user) {return doAction(db -> {

	JsonObject data = p.getJsonObject("data");

	Map<String, Object> r = getTable().HASH(data, "uuid", id);

	db.update(TarifDiff.class, r);

	storeFias(db, p, id);

	storeOktmo(db, p, id);

	storeEnumeration(db, p, id);

	logAction(db, user, id, VocAction.i.UPDATE);
    });}

    private static final Pattern RE_CODE = Pattern.compile("[0-9]+");

    @Override
    public JsonObject getEnumeration (JsonObject p) {return fetchData ((db, job) -> {

	JsonObject data = p.getJsonObject("data");

	NsiTable nsi = NsiTable.getNsiTable(data.getInt("registrynumber"));

	String label = nsi.getLabelField().getfName();

	Select select = db.getModel ()
            .select  (nsi, "AS root", "code AS id", label + " AS label")
	    .where("isactual", 1)
            .orderBy ("code")
            .limit (0, 50);

        StringBuilder sb = new StringBuilder ();
        StringTokenizer st = new StringTokenizer (p.getString ("search", ""));

        while (st.hasMoreTokens ()) {

            final String token = st.nextToken ();

            if (sb.length () == 0 && RE_CODE.matcher (token).matches ()) {
                select.and ("code", token);
            }
            else {
                sb.append (token);
                sb.append ('%');
            }
        }

        if (sb.length () > 0) {
            select.and (label + " LIKE", sb.toString ());
        }

        db.addJsonArrays (job, select);

    });}

    private void storeFias(DB db, JsonObject p, Object id) throws SQLException {

	JsonObject data = p.getJsonObject("data");

	if (!data.containsKey("fias") || data.isNull("fias")) {
	    return;
	}

	TarifDiffFias.store(db, id.toString(),
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

    private void storeOktmo(DB db, JsonObject p, Object id) throws SQLException {

	JsonObject data = p.getJsonObject("data");

	if (!data.containsKey("oktmo") || data.isNull("oktmo")) {
	    return;
	}

	TarifDiffOktmo.store(db, id.toString(),
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


    private void storeEnumeration(DB db, JsonObject p, Object id) throws SQLException {
	JsonObject data = p.getJsonObject("data");

	if (!data.containsKey("enumeration") || data.isNull("enumeration")) {
	    return;
	}

	TarifDiffNsi.store(db, id.toString(),
	    data.getJsonArray("enumeration").getValuesAs(JsonString.class).stream()
		.map((t) -> {
		    return DB.HASH("code", t.getString());
		})
		.collect(Collectors.toList())
	);
    }

    private void selectEnumeration(DB db, JsonObjectBuilder job, Select select) throws SQLException {

	final Model m = db.getModel();

	Map<Integer, Map<String, Object>> nsi2o = new HashMap<>();
	Map<UUID, Map<String, Object>> idx = new HashMap<>();

	db.forEach(select, (rs) -> {
		Map<String, Object> r = db.HASH(rs);

		Object nsiitem = r.get("vc_diff.nsiitem");

		if (nsiitem == null) {
		    return;
		}

		Integer registrynumber = ((Long) nsiitem).intValue();

		nsi2o.put(registrynumber, r);
		idx.put((UUID) r.get("uuid"), r);
	    }
	);

	JsonArrayBuilder nsi = Json.createArrayBuilder();

	for (Map.Entry<Integer, Map<String, Object>> entry : nsi2o.entrySet()) {

	    Integer registrynumber = entry.getKey();

	    NsiTable nsiTable = NsiTable.getNsiTable(registrynumber);

	    db.forEach(
		m.select(TarifDiffNsi.class, "AS root", "*")
		    .toOne(nsiTable, "AS enumeration", "code AS id", nsiTable.getLabelField().getfName() + " AS label")
			.where("isactual", 1)
			.on("root.code_vc_nsi = enumeration.code")
		    .where("uuid IN", idx.keySet().toArray())
		    .orderBy("enumeration.code")
		, (t) -> {
		    Map<String, Object> r = db.HASH(t);
		    nsi.add(DB.to.json(r));
		}
	    );
	}

	job.add("enumeration", nsi);
    }
}