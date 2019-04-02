package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.Map;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.TarifLegalAct;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.LegalAct;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;
import ru.eludia.products.mosgis.rest.api.TarifLegalActLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class TarifLegalActImpl extends BaseCRUD<TarifLegalAct> implements TarifLegalActLocal {

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return;

        select.and (LegalAct.c.DOCNUMBER.lc () + " LIKE ?%", searchString.toUpperCase ());
        
    }
    
    private void applySearch (final Search search, Select select) {        

        if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

	final Model m = ModelHolder.getModel();

	JsonObject data = p.getJsonObject("data");

	Select select = m.select (LegalAct.class, "AS root", "*")
	    .toOne(TarifLegalAct.class, "AS tla", "*")
		.where("uuid", data.getString("uuid_tf", null))
		.on("root.uuid = tla.uuid_legal_act")
	    .orderBy(LegalAct.c.APPROVEDATE.lc() + " DESC")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

	db.addJsonArrayCnt (job, select);
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

	final Model m = ModelHolder.getModel();

	final JsonObject item = db.getJsonObject (m
            .get (TarifLegalAct.class, id, "AS root", "*")
            .toOne(LegalAct.class, "AS la", "*").on()
        );

        job.add ("item", item);
    });}

    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db, job) -> {

        Map<String, Object> data = getData (p);

        db.upsert(getTable(), data);
    });}

    @Override
    public JsonObject doUpdate(String id, JsonObject p, User user) {return doAction(db -> {

	JsonObject data = p.getJsonObject("data");

	Map<String, Object> r = getTable().HASH(data, "uuid", id);

	db.upsert(getTable(), r);
    });}

    @Override
    public JsonObject doDelete (String id, JsonObject p, User user) {return doAction ((db) -> {

        db.delete(db.getModel().select(getTable(), "uuid")
	    .where("uuid_legal_act", id)
	    .and("uuid", p.getJsonObject("data").getString("uuid_tf", null))
        );
    });}
}