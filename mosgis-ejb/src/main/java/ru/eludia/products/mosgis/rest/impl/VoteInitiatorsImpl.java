package ru.eludia.products.mosgis.rest.impl;

import java.util.Map;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.VoteInitiator;
import ru.eludia.products.mosgis.db.model.tables.VoteInitiatorLog;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocol;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.VoteInitiatorsLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.Search;

@Stateless
public class VoteInitiatorsImpl extends BaseCRUD<VoteInitiator> implements VoteInitiatorsLocal {
    
    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocAction.addTo (jb);
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            db.addJsonArrays (jb,
                    
                ModelHolder.getModel ()
                    .select (VocOrganization.class, "uuid AS id", "label")
                    .orderBy ("label")
                    .and ("uuid", ModelHolder.getModel ().select (VotingProtocol.class, "uuid_org").where ("is_deleted", 0)),

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
            .where ("uuid_protocol", p.getJsonObject("data").getJsonString("protocol_uuid").getString ())
            .toMaybeOne (VoteInitiatorLog.class         ).on ()
            .and ("uuid_org", user.getUuidOrg ())
            .orderBy ("root.uuid")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        //applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}
    
    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db, job) -> {

        final Table table = getTable ();

        Map<String, Object> data = getData (p);

        Object insertId = db.insertId (table, data);
        
        job.add ("id", insertId.toString ());
        
        logAction (db, user, insertId, VocAction.i.CREATE);

    });}

    @Override
    public JsonObject getItem(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
