package ru.eludia.products.mosgis.rest.impl;

import java.util.Map;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.CheckPlan;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.tables.PlannedExamination;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.PlannedExaminationsLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PlannedExaminationsImpl extends BaseCRUD<PlannedExamination> implements PlannedExaminationsLocal {

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

        select.and ("subject_label LIKE %?%", searchString);
        
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
    
    @Override
    public JsonObject select(JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select(getTable (), "AS root", "*", "uuid AS id")
                .where ("check_plan_uuid", p.getJsonObject("data").getString("plan_uuid"))
                .orderBy ("root.numberinplan")
                .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);
        
    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {
        
        JsonObject item = db.getJsonObject(ModelHolder.getModel ().get (PlannedExamination.class, id, "AS root", "*")
                .toOne(CheckPlan.class, "AS plan", "shouldberegistered", "sign").on ()
        );
        
        job.add ("item", item);
        
    });}

    @Override
    public JsonObject getVocs() {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocAction.addTo (jb);
        
        final MosGisModel model = ModelHolder.getModel ();

        try (DB db = model.getDb ()) {
            
            db.addJsonArrays (jb,
                    
                NsiTable.getNsiTable (65).getVocSelect (),
                NsiTable.getNsiTable (68).getVocSelect (),
                NsiTable.getNsiTable (71).getVocSelect ()
                    
            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }
    
    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db, job) -> {

        final Table table = getTable ();

        Map<String, Object> data = getData (p);
        
        data.put (PlannedExamination.c.REGULATOR_UUID.lc (), user.getUuidOrg ());

        Object insertId = db.insertId (table, data);
        
        job.add ("id", insertId.toString ());
        
        logAction (db, user, insertId, VocAction.i.CREATE);

    });}

}
