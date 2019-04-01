package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.Map;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.annotation.Resource;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.PremiseUsageTarif;
import ru.eludia.products.mosgis.db.model.tables.PremiseUsageTarifLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.PremiseUsageTarifOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiation;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationOperator;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationValueKindType;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.PremiseUsageTarifLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PremiseUsageTarifImpl extends BaseCRUD<PremiseUsageTarif> implements PremiseUsageTarifLocal {

    @Resource (mappedName = "mosgis.inExportPremiseUsageTarifsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }
    @Override
    protected void publishMessage (VocAction.i action, String id_log) {

        switch (action) {
            case APPROVE:
            case ANNUL:
                super.publishMessage (action, id_log);
            default:
                return;
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

        select.and (PremiseUsageTarif.c.NAME.lc () + " LIKE ?%", searchString.toUpperCase ());
        
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
    
    private void checkFilter (JsonObject data, PremiseUsageTarif.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

	final Model m = ModelHolder.getModel();

        Select select = m.select (getTable (), "AS root", "*")
            .toMaybeOne (PremiseUsageTarifLog.class).on ()
	    .toMaybeOne(VocOrganization.class, "AS org", "label").on("root.uuid_org = org.uuid")
            .orderBy ("root.datefrom DESC")
            .orderBy ("root.dateto DESC")
            .orderBy ("root.name")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);


	Map<Object, Map<String, Object>> idx = db.getIdx(select, "uuid");

	db.addJsonArrays(job,
	    m.select(PremiseUsageTarifOktmo.class, "AS oktmos", "*")
		.toOne(VocOktmo.class, "AS vc_oktmo", "code").on()
		.where("uuid IN", idx.keySet().toArray())
		.orderBy("vc_oktmo.code")
	);
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

	final Model m = ModelHolder.getModel();

	final JsonObject item = db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
            .toMaybeOne (PremiseUsageTarifLog.class, "AS log").on ()
	    .toMaybeOne (OutSoap.class, "err_text").on ("log.uuid_out_soap=out_soap.uuid")
            .toOne      (VocOrganization.class, "AS org", "label").on ("root.uuid_org=org.uuid")
        );

        job.add ("item", item);
        
        db.addJsonArrays (job, 
            m.select (PremiseUsageTarifOktmo.class, "AS oktmos", "*")
                .toOne (VocOktmo.class, "AS vc_oktmo", "code", "site_name", "id").on ()
		.where("uuid", id)
                .orderBy ("vc_oktmo.code")
        );        

        VocGisStatus.addTo (job);
        VocAction.addTo (job);

    });}

    @Override
    public JsonObject getVocs () {

	JsonObjectBuilder jb = Json.createObjectBuilder();

	VocGisStatus.addTo(jb);
	VocAction.addTo(jb);

	return jb.build();
    }

    @Override
    public JsonObject doCreate(JsonObject p, User user) { return doAction((db, job) -> {

	final Table table = getTable();

	Map<String, Object> data = getData(p);

	if (!data.containsKey(UUID_ORG)) {
	    data.put(UUID_ORG, user.getUuidOrg());
	}

	Object insertId = db.insertId(table, data);

	job.add("id", insertId.toString());

	if (p.getJsonObject("data").containsKey("oktmo")) {

	    PremiseUsageTarifOktmo.store(db, insertId.toString()
		, p.getJsonObject("data").getJsonArray("oktmo").getValuesAs(JsonString.class).stream()
		    .map((t) -> {
			return DB.HASH("id", t.getString());
		    })
		    .collect(Collectors.toList())
	    );
	}

	logAction(db, user, insertId, VocAction.i.CREATE);

    });}

    @Override
    public JsonObject doUpdate(String id, JsonObject p, User user) {return doAction(db -> {

	JsonObject data = p.getJsonObject("data");

	Map<String, Object> r = getTable().HASH(data, "uuid", id);

	db.update(PremiseUsageTarif.class, r);

	if (data.containsKey("oktmo")) {

	    PremiseUsageTarifOktmo.store(db, id
		, data.getJsonArray("oktmo").getValuesAs(JsonString.class).stream()
		    .map((t) -> {
			return DB.HASH("id", t.getString());
		    })
		    .collect(Collectors.toList())
	    );
	}

	logAction(db, user, id, VocAction.i.UPDATE);
    });}

    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (EnTable.c.UUID,               id,
            PremiseUsageTarif.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

    });}
    
    @Override
    public JsonObject doAlter (String id, User user) {return doAction ((db) -> {
                
        final Map<String, Object> r = HASH (
            EnTable.c.UUID,               id,
            PremiseUsageTarif.c.ID_CTR_STATUS,  VocGisStatus.i.PROJECT.getId ()
        );
                
        db.update (getTable (), r);
        
        logAction (db, user, id, VocAction.i.ALTER);
        
    });}

    @Override
    public JsonObject doAnnul (String id, JsonObject p, User user) {return doAction ((db) -> {

        final Map<String, Object> r = getData(p,
            EnTable.c.UUID,               id,
            PremiseUsageTarif.c.ID_CTR_STATUS,  VocGisStatus.i.PENDING_RQ_ANNULMENT.getId ()
        );

        db.update (getTable (), r);

        logAction (db, user, id, VocAction.i.ANNUL);

    });}
}