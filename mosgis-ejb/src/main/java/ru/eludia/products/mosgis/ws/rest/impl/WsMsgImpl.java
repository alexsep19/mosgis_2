package ru.eludia.products.mosgis.ws.rest.impl;

import java.sql.SQLException;
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
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.Sender;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.rest.api.WsMsgLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.Base;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsMsgImpl extends Base<WsMessages> implements WsMsgLocal {


    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");

    }
/*
    private void applySimpleSearch (final SimpleSearch search, Select select) {

        filterOffDeleted (select);

        final String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return;

        select.and ("label_uc LIKE %?%", searchString.toUpperCase ());
        
    }
    */
    private void applySearch (final Search search, Select select) {        

        /*if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        else*/ 
        if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }

    }
    
    @Override
    public JsonObject select(JsonObject p) {
        return fetchData((db, job) -> {

            Select select = ModelHolder.getModel().select(getTable(), "*", "uuid AS id")
                    .toOne(Sender.class, "AS sender", "label AS sender_name").on()
                    .toMaybeOne(VocOrganization.class, "AS org", "shortname AS org_name").on()
                    .orderBy(WsMessages.c.REQUEST_TIME.lc() + " DESC")
                    .limit(p.getInt("offset"), p.getInt("limit"));

            applySearch(Search.from(p), select);
            db.addJsonArrayCnt(job, select);

        });
    }
    
    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocAsyncRequestState.addTo(jb);
        
        final MosGisModel model = ModelHolder.getModel ();

        try (DB db = model.getDb ()) {
            
            db.addJsonArrays (jb,
            
                model
                    .select (Sender.class, "uuid AS id", "label")                    
                    .orderBy ("label")
                    .where ("is_deleted", 0)
            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }
    
    @Override
    public JsonObject getRq(String id) {

        JsonObjectBuilder jb = Json.createObjectBuilder();

        try (DB db = ModelHolder.getModel().getDb()) {

            jb.add("xml", db.getString(ModelHolder.getModel()
                    .select(WsMessages.class, "request")
                    .where("uuid", id)
            ));

        } catch (SQLException ex) {
            throw new InternalServerErrorException(ex);
        }

        return jb.build();

    }

    @Override
    public JsonObject getRp(String id) {

        JsonObjectBuilder jb = Json.createObjectBuilder();

        try (DB db = ModelHolder.getModel().getDb()) {

            jb.add("xml", db.getString(ModelHolder.getModel()
                    .select(WsMessages.class, "response")
                    .where("uuid", id)
            ));

        } catch (SQLException ex) {
            throw new InternalServerErrorException(ex);
        }

        return jb.build();

    }

}