package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocPersonLog;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.PersonLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PersonImpl extends BaseCRUD<VocPerson> implements PersonLocal {
    
    private static final Logger logger = Logger.getLogger (PersonImpl.class.getName ());    
    
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

        select.and ("label_uc LIKE %?%", searchString.toUpperCase ());
        
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

        String uuidOrg = p.getString("uuid_org", null);

        uuidOrg = uuidOrg instanceof String? uuidOrg : user.getUuidOrg();

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .toOne (VocOrganization.class, "AS org", "label").on ()
            .toMaybeOne (VocPersonLog.class         ).on ()
            .and ("uuid_org", uuidOrg)
            .orderBy ("org.label")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}
    
    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "*")
            .toOne      (VocOrganization.class,        "label").on ()
            .toMaybeOne (VocPersonLog.class        ).on ()
        ));

    });}
    
    @Override
    public JsonObject getLog (String id, JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Table logTable = ModelHolder.getModel ().getLogTable (getTable ());
        
        if (logTable == null) return;
        
        Select select = ModelHolder.getModel ().select (logTable, "AS log", "*", "uuid AS id")
            .and ("uuid_object", id)
            .toMaybeOne (VocUser.class, "label").on ()
            .orderBy ("log.ts DESC")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        logTable.getColumns ().forEachEntry (0, (i) -> {
            final Col value = i.getValue ();
            if (!(value instanceof Ref)) return;
            Ref ref = (Ref) value;
            switch (i.getKey ()) {
                case "uuid_user":
                    return;
                default:
                    final Table targetTable = ref.getTargetTable ();
                    if (!targetTable.getColumns ().containsKey ("label")) return;
                    final String name = ref.getName ();
                    StringBuilder sb = new StringBuilder ("AS ");
                    if (name.startsWith ("uuid_")) {
                        sb.append (name.substring (5));
                    }
                    else if (name.startsWith ("id_")) {
                        sb.append (name.substring (3));
                    }
                    else {
                        sb.append (name);
                    }                            
                    select.toMaybeOne (targetTable, sb.toString (), "label").on (ref.getName ());
            }
        });

        db.addJsonArrayCnt (job, select);
        
    });}
    
    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocAction.addTo (jb);
        
        final MosGisModel model = ModelHolder.getModel ();

        try (DB db = model.getDb ()) {
            
            db.addJsonArrays (jb,
                    
                NsiTable.getNsiTable (95).getVocSelect (),
                    
                model
                    .select (VocOkei.class, "code AS id", "national AS label")
                    .orderBy ("national"),
                    
                model
                    .select (VocOrganization.class, "uuid AS id", "label")                    
                    .orderBy ("label")
                    .and ("uuid", model.select (VocPerson.class, "uuid_org").where ("is_deleted", 0))

            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }
    
}
