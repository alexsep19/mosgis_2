package ru.eludia.products.mosgis.rest.impl;

import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Predicate;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.WorkingList;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.WorkingListLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
public class WorkingListImpl extends BaseCRUD<WorkingList> implements WorkingListLocal {
    
    private static final Logger logger = Logger.getLogger (WorkingListImpl.class.getName ());    
       
    private void filterOffDeleted (Select select) {
        select.and (EnTable.c.IS_DELETED, Operator.EQ, 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {
        
        final Map<String, Predicate> filters = search.getFilters ();
        
        Predicate dt = filters.get ("dt");
        
        if (dt != null) {
            final Object [] v = dt.getValues ();
            filters.put (WorkingList.c.DT_FROM.lc (), new Predicate ("<=", v));
            filters.put (WorkingList.c.DT_TO.lc (),   new Predicate (">=", v));
            filters.remove ("dt");
        }
        
        search.apply (select);
        
        if (!filters.containsKey ("is_deleted")) filterOffDeleted (select);
        
    }
    
    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String s = search.getSearchString ();
        
        if (s == null || s.isEmpty ()) return;
        
        final String uc = s.toUpperCase ();

        select.andEither ("owner_label_uc LIKE %?%", uc).or ("label", s);

    }
    
    private void applySearch (final Search search, Select select) {        

        if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else {
            if (search instanceof SimpleSearch) applySimpleSearch  ((SimpleSearch) search, select);
            filterOffDeleted (select);        
        }
        
    }
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        final Model m = ModelHolder.getModel ();

        Select select = m.select (WorkingList.class, "*", EnTable.c.UUID.lc () + " AS id")
            .orderBy (WorkingList.c.DT_FROM.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}
    
    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        job.add ("item", db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
//            .toOne (Premise.class, "AS p", "*").on ()                
//            .toOne (House.class, "AS h", "address").on ()
        ));

//        db.addJsonArrays (job,
//            m
//                .select (VocProtertyDocumentType.class, "id", "label")
//                .orderBy ("label")
//        );

    });}        
    
}
