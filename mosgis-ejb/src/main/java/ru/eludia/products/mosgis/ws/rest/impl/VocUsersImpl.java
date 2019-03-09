package ru.eludia.products.mosgis.ws.rest.impl;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Predicate;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.VocUsersLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.Base;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocUsersImpl extends Base<VocUser> implements VocUsersLocal {

    private static final Logger logger = Logger.getLogger (VocUsersImpl.class.getName ());

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");

        Predicate code_vc_nsi_20 = search.getFilters ().get ("code_vc_nsi_20");

        if (code_vc_nsi_20 != null) select.and ("uuid_org", ModelHolder.getModel ().select (VocOrganizationNsi20.class, "uuid").and ("code", code_vc_nsi_20));

    }
    
    private static final Pattern RE = Pattern.compile (".*[А-ЯЁа-яё].*");
    
    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return;

        Matcher matcher = RE.matcher (searchString);

        if (matcher.matches ()) {            
            select.and ("label_uc LIKE %?%", searchString.toUpperCase ());
        }
        else {
            select.and ("login LIKE ?%", searchString);
        }
        
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
    public JsonObject select (JsonObject p) {
                
        Select select = ModelHolder.getModel ().select (VocUser.class, "*", "uuid AS id")
            .and ("is_deleted", 0)
            .orderBy ("vc_users.label")
            .toMaybeOne (VocOrganization.class, "label").on ()
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        JsonObject data = p.getJsonObject ("data");
        if (data != null) select.and ("uuid_org", data.getString ("uuid_org", null));

        applySearch (Search.from (p), select);

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select);
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }    
/*
    @Override
    public JsonObject getItem (String id) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = model.getDb ()) {

            JsonObject item = db.getJsonObject (model
                .get (VocOrganization.class, id, "*")
                .toMaybeOne (VocOrganizationTypes.class, "label").on ()
            );

            jb.add ("item", item);
            
            db.addJsonArrays (jb, model.select (VocOrganizationNsi20.class, "code").where ("uuid", id));

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();

    }
*/
    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            db.addJsonArrays (jb, 
                    
                ModelHolder.getModel ()
                    .select (VocOrganization.class, "uuid AS id", "label")
                    .and ("uuid", ModelHolder.getModel ().select (VocUser.class, "uuid_org"))
                    .orderBy ("label"),
                
                NsiTable.getNsiTable (20).getVocSelect ()
            
            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }
    
    private void croak (SQLException ex) {
        if (ex.getErrorCode () == 1) throw new ValidationException ("login", "Этот login уже занят");
        throw new InternalServerErrorException (ex);
    }

    @Override
    public JsonObject doCreate (JsonObject p) {return fetchData ((db, job) -> {
        
        try {
            job.add ("uuid", db.insertId (getTable (), getData (p)).toString ());
        } 
        catch (SQLException ex) {
            croak (ex);
        }        
        
    });}

    @Override
    public JsonObject doUpdate (String id, JsonObject p) {return doAction ((db) -> {
                
        try {
            db.update (getTable (), getData (p,
                "uuid", id
            ));
        }            
        catch (SQLException ex) {
            croak (ex);
        }        
                        
    });}

    @Override
    public JsonObject doDelete (String id) {return doAction ((db) -> {
        
        db.update (getTable (), HASH (
            "uuid",        id,
            "is_deleted",  1,
            "login",       ""
        ));
        
    });}

    @Override
    public JsonObject doSetPassword (String id, String password) {return doAction ((db) -> {
        
        UUID salt = UUID.randomUUID ();
        
        db.update (getTable (), HASH (
            "uuid",   id,
            "salt",   salt,
            "sha1",   VocUser.encrypt (salt, password)
        ));
        
    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {
        job.add ("item", db.getJsonObject (ModelHolder.getModel ().get (getTable (), id, "*")));
    });}

}