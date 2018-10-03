package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWork;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWorkLog;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWorkNsi67;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.OrganizationWorkLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
public class OrganizationWorkImpl extends BaseCRUD<OrganizationWork> implements OrganizationWorkLocal {

    @Resource (mappedName = "mosgis.inNsiOrganizationWorksQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }
    
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
        
        final int limit = p.getInt ("limit");
        final int offset = p.getInt ("offset");

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .toOne (VocOrganization.class, "AS org", "label").on ()
            .toMaybeOne (OrganizationWorkLog.class         ).on ()
            .toMaybeOne (OutSoap.class,           "err_text").on ()
            .and ("uuid_org", user.getUuidOrg ())
            .orderBy ("org.label")
            .orderBy ("root.label")
            .limit (offset, limit);

        applySearch (Search.from (p), select);
        
        final int cnt = db.getCnt (select);

        job.add ("cnt", cnt);        
        
        JsonArrayBuilder ab = Json.createArrayBuilder ();
        
        if (cnt > offset) {

            List<JsonObjectBuilder> jobs = new ArrayList <> (limit);
            Map<UUID, List<String>> uuid2codes = new HashMap <> (limit);
            Map<UUID, JsonObjectBuilder> uuid2jb = new HashMap <> (limit);

            db.forEach (db.toQP (select) , rs -> {
                final JsonObjectBuilder jsonObjectBuilder = db.getJsonObjectBuilder (rs);
                jobs.add (jsonObjectBuilder);
                final UUID uuid = DB.to.UUID (rs.getBytes ("uuid"));
                uuid2codes.put (uuid, new ArrayList <> ());
                uuid2jb.put (uuid, jsonObjectBuilder);
            });       

            db.forEach (ModelHolder.getModel ().select (OrganizationWorkNsi67.class, "uuid", "code").where ("uuid IN", uuid2codes.keySet ().toArray ()), (rs) -> {
                uuid2codes.get (DB.to.UUID (rs.getBytes ("uuid"))).add (rs.getString ("code"));
            });

            uuid2codes.forEach ((uuid, codes) -> {
                JsonArrayBuilder a = Json.createArrayBuilder ();
                codes.forEach ((s) -> a.add (s));
                uuid2jb.get (uuid).add ("codes_nsi_67", a);
            });

            for (JsonObjectBuilder jsonObjectBuilder: jobs) {
                ab.add (jsonObjectBuilder);
            }
            
        }

        job.add (select.getTableAlias (), ab);

    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((DB db, JsonObjectBuilder job) -> {

        final MosGisModel model = ModelHolder.getModel ();
        
        final Select get = model
            .get (getTable (), id, "*")
            .toOne      (VocOrganization.class,        "label").on ()
            .toMaybeOne (OrganizationWorkLog.class            ).on ()
            .toMaybeOne (OutSoap.class,             "err_text").on ();
        
        QP qp = db.toQP (get);
        
        JsonObjectBuilder [] jobs = new JsonObjectBuilder [1];
        
        db.forFirst (qp, (rs) -> {
            jobs [0] = db.getJsonObjectBuilder (rs);
        });
        
        JsonArrayBuilder a = Json.createArrayBuilder ();
        
        db.forEach (model.select (OrganizationWorkNsi67.class, "code").where ("uuid", id), (rs) -> {
            a.add (rs.getString (1));
        });

        jobs [0].add ("codes_nsi_67", a);

        job.add ("item", jobs [0]);

    });}
    
    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db) -> {

        final Table table = getTable ();

        Map<String, Object> data = getData (p);

        if (table.getColumn (UUID_ORG) != null && !data.containsKey (UUID_ORG)) data.put (UUID_ORG, user.getUuidOrg ());

        Object insertId = db.insertId (table, data);
        
        setNsi67 (db, insertId, p);

        logAction (db, user, insertId, VocAction.i.CREATE);

    });}
    
    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        db.update (getTable (), getData (p,
            "uuid", id
        ));
        
        setNsi67 (db, id, p);

        logAction (db, user, id, VocAction.i.UPDATE);
                        
    });}    

    protected void setNsi67 (DB db, Object insertId, JsonObject p) throws SQLException {
        
        db.dupsert (
            OrganizationWorkNsi67.class,
            HASH ("uuid", insertId),
            p.getJsonObject ("data").getJsonArray ("code_vc_nsi_67").stream ().map ((t) -> {return HASH ("code", ((JsonString) t).getString ());}).collect (Collectors.toList ()),
            "code"
        );
        
    }

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        final MosGisModel model = ModelHolder.getModel ();

        try (DB db = model.getDb ()) {
            
            db.addJsonArrays (jb,
                    
                NsiTable.getNsiTable (56).getVocSelect (),
                
                NsiTable.getNsiTable (67).getVocSelect (),
                                        
                model
                    .select (VocOrganization.class, "uuid AS id", "label")                    
                    .orderBy ("label")
                    .and ("uuid", model.select (OrganizationWork.class, "uuid_org").where ("is_deleted", 0)),

                model
                    .select (VocAsyncEntityState.class, "id", "label")                    
                    .orderBy ("label"),

                model
                    .select (VocOkei.class, "code AS id", "national AS label")
                    .orderBy ("national")

            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }
    
}
