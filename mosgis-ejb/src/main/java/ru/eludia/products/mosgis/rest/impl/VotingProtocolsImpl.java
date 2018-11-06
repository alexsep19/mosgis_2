package ru.eludia.products.mosgis.rest.impl;

import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocol;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocolLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.VotingProtocolsLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
public class VotingProtocolsImpl extends BaseCRUD<VotingProtocol> implements VotingProtocolsLocal {

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
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "*")
            .toMaybeOne (VotingProtocolLog.class           ).on ()
            .toMaybeOne (OutSoap.class,             "err_text").on ()
        ));

    });}

    @Override
    public JsonObject select(JsonObject p, User user) {
        
        Model m = ModelHolder.getModel ();
        
        Select select = m.select (VotingProtocol.class, "*", "uuid AS id")
            //.toOne (Premise.class, "AS premise", "*").where ("uuid_house", p.getJsonObject ("data").getString ("uuid_house", null)).on ()
            .orderBy("protocoldate")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select);
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
        
    }
    
}
