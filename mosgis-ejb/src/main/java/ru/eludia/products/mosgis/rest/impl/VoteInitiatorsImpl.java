package ru.eludia.products.mosgis.rest.impl;

import java.util.Map;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.PropertyDocument;
import ru.eludia.products.mosgis.db.model.tables.VoteInitiator;
import ru.eludia.products.mosgis.db.model.tables.VoteInitiatorLog;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocol;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.VoteInitiatorsLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
public class VoteInitiatorsImpl extends BaseCRUD<VoteInitiator> implements VoteInitiatorsLocal {
    
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
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocAction.addTo (jb);
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            db.addJsonArrays (jb,
                    
                ModelHolder.getModel ()
                    .select (VocOrganization.class, "uuid AS id", "label")
                    .orderBy ("label"),

                ModelHolder.getModel ()
                    .select (VocAsyncEntityState.class, "id", "label")
                    .orderBy ("label")

            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
       
        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .toMaybeOne (PropertyDocument.class, "AS prop", "uuid_person_owner").on ()
            .toMaybeOne (VocOrganization.class, "AS org", "id_type").on ()
            .toMaybeOne (VoteInitiatorLog.class         ).on ()
            .where ("uuid_protocol", p.getJsonObject("data").getJsonString("protocol_uuid").getString ())
            .orderBy ("root.uuid")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}
    
    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db, job) -> {
        
        final Table table = getTable ();

        Map<String, Object> data = getData (p);

        Object insertId = db.insertId (table, data);
        
        job.add ("id", insertId.toString ());
        
        if (data.containsKey("uuid_ind")) {
            
            JsonObject personId = db.getJsonObject(ModelHolder.getModel()
                    .get(getTable (), insertId.toString ())
                    .toOne (PropertyDocument.class, "uuid_person_owner AS id").on ()
            );
            
            job.add ("person", personId);
            
        }
        else if (data.containsKey("uuid_org")) {
            
            JsonObject org = db.getJsonObject(ModelHolder.getModel()
                    .get(getTable (), insertId.toString ())
                    .toOne (VocOrganization.class, "id_type AS type", "uuid AS id").on ()
            );
            
            job.add ("org", org);
            
        }
        
        logAction (db, user, insertId, VocAction.i.CREATE);

    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        JsonObject item = db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "*")
            .toOne (VotingProtocol.class, "AS protocol", "fiashouseguid").on ()
        );
        
        job.add ("item", item);
        
        final String fiashouseguid = item.getString ("protocol.fiashouseguid");
        VocBuilding.addCaCh (db, job, fiashouseguid);

    });}
    
}
