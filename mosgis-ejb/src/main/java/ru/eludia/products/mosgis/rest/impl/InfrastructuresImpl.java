package ru.eludia.products.mosgis.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Infrastructure;
import ru.eludia.products.mosgis.db.model.tables.VocNsi38;
import ru.eludia.products.mosgis.db.model.tables.VocNsi40;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.InfrastructuresLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InfrastructuresImpl extends BaseCRUD<Infrastructure> implements InfrastructuresLocal {

    private final String LABEL_FIELD_NAME_NSI_33 = "f_c8e745bc63";
    
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

        select.and ("name LIKE %?%", searchString);
        
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
                .orderBy ("root.name")
                .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);
        
    });}

    @Override
    public JsonObject getItem(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocAction.addTo (jb);
        
        final MosGisModel model = ModelHolder.getModel ();

        try (DB db = model.getDb ()) {
            
            db.addJsonArrays (jb,

                NsiTable.getNsiTable (3).getVocSelect (),
                NsiTable.getNsiTable (34).getVocSelect (),
                NsiTable.getNsiTable (35).getVocSelect (),
                NsiTable.getNsiTable (37).getVocSelect (),
                NsiTable.getNsiTable (39).getVocSelect (),
                
                VocNsi38.getVocSelect (),
                VocNsi40.getVocSelect (),
                
                model
                    .select (NsiTable.getNsiTable (33), "code AS id", LABEL_FIELD_NAME_NSI_33 + " AS label")
                    .where (LABEL_FIELD_NAME_NSI_33 + " IS NOT NULL")
                    .and ("isactual", 1)
                    .orderBy ("code"),
                    
                model
                    .select (VocGisStatus.class, "id", "label")
                    .orderBy ("id")

            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }
    
    
    
}
