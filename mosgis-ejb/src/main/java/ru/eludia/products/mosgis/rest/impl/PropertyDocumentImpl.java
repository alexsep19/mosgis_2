package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.Premise;
import ru.eludia.products.mosgis.db.model.tables.PropertyDocument;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocProtertyDocumentType;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.PropertyDocumentLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
public class PropertyDocumentImpl extends BaseCRUD<PropertyDocument> implements PropertyDocumentLocal {
    
    private static final Logger logger = Logger.getLogger (PropertyDocumentImpl.class.getName ());    
    
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

        select.and ("label LIKE %?%", searchString.toUpperCase ());
        
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
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        final Model m = ModelHolder.getModel ();

        Select select = m.select (getTable (), "AS root", "*", "uuid AS id")
            .toOne (Premise.class, "AS premise", "*").where ("uuid_house", p.getJsonObject ("data").getString ("uuid_house", null)).on ()
            .toMaybeOne (VocOrganization.class, "AS org", "label").on ("org.uuid=root." + PropertyDocument.c.UUID_ORG_OWNER.lc ())
            .toMaybeOne (VocPerson.class, "AS person", "label").on ()
/*                
            .and (PropertyDocument.c.UUID_PREMISE.toString ().toLowerCase (), 
                m.select (Premise.class, "id").where ("uuid_house", p.getJsonObject ("data").getString ("uuid_house", null)))
*/
            .orderBy ("premise.label")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}
    
    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        job.add ("item", db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
            .toOne (Premise.class, "AS p", "*").on ()                
            .toOne (House.class, "AS h", "address").on ()
            .toMaybeOne (VocOrganization.class, "AS org", "label").on ("org.uuid=root." + PropertyDocument.c.UUID_ORG_OWNER.lc ())
            .toMaybeOne (VocPerson.class, "AS person", "label").on ()
        ));

        db.addJsonArrays (job,
            m
                .select (VocProtertyDocumentType.class, "id", "label")
                .orderBy ("label")
        );

    });}        
    
}
