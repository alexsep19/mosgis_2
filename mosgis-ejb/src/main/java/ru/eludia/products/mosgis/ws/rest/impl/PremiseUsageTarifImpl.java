package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.Map;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.PremiseUsageTarif;
import ru.eludia.products.mosgis.db.model.tables.PremiseUsageTarifLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.PremiseUsageTarifOktmo;
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
/*
    @Resource (mappedName = "mosgis.inPremiseUsageTarifsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }
*/
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
        
        final JsonObject item = db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "AS root", "*")
            .toMaybeOne (PremiseUsageTarifLog.class, "AS log").on ()
            // .toMaybeOne (OutSoap.class, "err_text").on ("log.uuid_out_soap=out_soap.uuid")
            .toOne      (VocOrganization.class, "AS org", "label").on ("root.uuid_org=org.uuid")
        );

        job.add ("item", item);
        
        db.addJsonArrays (job, 
            ModelHolder.getModel ().select (PremiseUsageTarifOktmo.class, "AS oktmos", "*")
                .toOne (VocOktmo.class, "AS vc_oktmo", "code", "site_name").on ()
		.where("uuid", id)
                .orderBy ("vc_oktmo.code")
        );        

        VocGisStatus.addTo (job);
        VocAction.addTo (job);

    });}

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        VocAction.addTo (jb);
	VocGisStatus.addTo(jb);
	VocAction.addTo(jb);
        
        return jb.build ();
        
    }
/*    
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
*/    
}