package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.Map;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
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

//        select.and (LegalAct.c.DOCNUMBER.lc () + " LIKE ?%", searchString.toUpperCase ());
        
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
    
    private void checkFilter (JsonObject data, TarifLegalAct.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

	final Model m = ModelHolder.getModel();

	JsonObject data = p.getJsonObject("data");

	Select select = m.select (LegalAct.class, "AS root", "*")
	    .toOne(TarifLegalAct.class, "AS tla", "*")
		.where(TarifLegalAct.c.UUID_TF, data.getString(TarifLegalAct.c.UUID_TF.lc(), null))
		.and(EnTable.c.IS_DELETED, 0)
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
    public JsonObject doUpdate(String id, JsonObject p, User user) {return doAction(db -> {

	JsonObject data = p.getJsonObject("data");

	Map<String, Object> r = getTable().HASH(data, "uuid", id);

	db.update(TarifLegalAct.class, r);

	logAction(db, user, id, VocAction.i.UPDATE);
    });}
}