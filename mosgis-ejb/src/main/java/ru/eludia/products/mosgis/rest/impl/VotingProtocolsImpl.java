package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocol;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocolLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.VotingProtocolsLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
public class VotingProtocolsImpl extends BaseCRUD<VotingProtocol> implements VotingProtocolsLocal {

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

        select.and ("label_form_uc LIKE %?%", searchString);
        
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
       
        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .where ("fiashouseguid", p.getJsonObject("data").getJsonString("uuid_house").getString ())
            .toOne (VocGisStatus.class, "label AS status_label").on("id_prtcl_status_gis")
            .toMaybeOne (VotingProtocolLog.class         ).on ()
            .and ("uuid_org", user.getUuidOrg ())
            .orderBy ("root.id_prtcl_status_gis")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}
    
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
                    .orderBy ("label"),
                    
                ModelHolder.getModel ()
                    .select(VocGisStatus.class, "id", "label")
                    .orderBy ("id")

            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        JsonObject item = db.getJsonObject (ModelHolder.getModel ()
            .get (VotingProtocol.class, id, "AS root", "*")
            .toOne (VocGisStatus.class, "label AS status_label").on("id_prtcl_status_gis")
            .toOne (VocBuilding.class, "label AS address_label").on ()
            .toMaybeOne (House.class, "AS house", "uuid AS house_uuid").on ("root.fiashouseguid=house.fiashouseguid")
            .toMaybeOne (VotingProtocolLog.class           ).on ()
            .toMaybeOne (OutSoap.class,             "err_text").on ()
        ); 
        
        job.add ("item", item);
        
        final String fiashouseguid = item.getString ("fiashouseguid");    
        VocBuilding.addCaCh (db, job, fiashouseguid);

    });}
    
}