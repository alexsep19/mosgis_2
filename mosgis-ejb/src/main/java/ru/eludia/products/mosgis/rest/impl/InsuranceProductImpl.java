package ru.eludia.products.mosgis.rest.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.InsuranceProduct;
import ru.eludia.products.mosgis.db.model.tables.InsuranceProductLog;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationInsurance;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.InsuranceProductLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
public class InsuranceProductImpl extends BaseCRUD<InsuranceProduct> implements InsuranceProductLocal {

    @Resource (mappedName = "mosgis.inInsuranceProductsQueue")
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

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .toOne (VocOrganization.class,          "AS org", "label").on ()
            .toOne (VocOrganizationInsurance.class, "AS org_ins", "label").on ()
            .toMaybeOne (InsuranceProductLog.class                   ).on ()
            .toMaybeOne (OutSoap.class,                    "err_text").on ()
            .and ("uuid_org", user.getUuidOrg ())
            .orderBy ("org.label")
            .orderBy ("root.label")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (InsuranceProduct.class, id, "*")
            .toOne      (VocOrganization.class,                        "label").on ()
            .toOne      (VocOrganizationInsurance.class, "AS org_ins", "label").on ()
            .toMaybeOne (InsuranceProductLog.class                            ).on ()
            .toMaybeOne (OutSoap.class,                             "err_text").on ()
        ));

    });}

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            db.addJsonArrays (jb,
                    
                ModelHolder.getModel ()
                    .select (VocOrganizationInsurance.class, "id", "label")
                    .orderBy ("label"),
                    
                ModelHolder.getModel ()
                    .select (VocOrganization.class, "uuid AS id", "label")
                    .orderBy ("label")
                    .and ("uuid", ModelHolder.getModel ().select (InsuranceProduct.class, "uuid_org").where ("is_deleted", 0)),

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
    public JsonObject doCreate (JsonObject p, User user) {
        
        JsonObject file = p.getJsonObject ("file");
        
        String uuid = file.getString ("uuid", null);
        
        if (uuid != null) {
                
            doUpdate (uuid, Json.createObjectBuilder ().add ("data", file).build (), user);
            
            return Json.createObjectBuilder ().add ("id", uuid).build ();

        }
        
        return fetchData ((db, job) -> {

        db.begin ();
        
            final Object insertId = db.insertId (getTable (), HASH (
                "uuid_org",     user.getUuidOrg (),
                "insuranceorg", file.getString ("insuranceorg"),
                "name",         file.getString ("label"),
                "description",  file.getString ("description"),
                "mime",         file.getString ("type"),
                "len",          file.getInt    ("size")
            ));

            job.add ("id", insertId.toString ());
                        
        db.commit ();
        
    });}
        
    @Override
    public JsonObject doAppend (String id, JsonObject p, User user) {return doAction (db -> {
                
        byte [] bytes = Base64.getDecoder ().decode (p.getString ("chunk"));

        Connection cn = db.getConnection ();

        final String uuid = id.replace ("-", "").toUpperCase ();
                
        String action = null;

        try (PreparedStatement st = cn.prepareStatement ("SELECT body, DBMS_LOB.GETLENGTH(body), len, id_log FROM " + getTable ().getName () + " WHERE uuid = ? FOR UPDATE")) {

            st.setString (1, uuid);

            try (ResultSet rs = st.executeQuery ()) {

                if (rs.next ()) {

                    Blob blob  = rs.getBlob (1);
                    long  len  = rs.getLong (2);
                            
                    long total = rs.getLong (3);
                            
                    long newLen = len + bytes.length;
                            
                    if (newLen > total) throw new IllegalArgumentException ("Having " + len + " bytes, received " + bytes.length + " resulting to " + newLen + ", but expeced only " + total);
                            
                    if (newLen == total) {
                        String id_log = rs.getString (4);                            
                        action = rs.wasNull () ? "create" : "update";
                    }
                            
                    try (OutputStream os = blob.setBinaryStream (len + 1)) {
                        os.write (bytes);                            
                    }

                }

            }

        }                    
                
        if (action != null) logAction (db, user, id, action);

    });}

    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {

        final Map<String, Object> data = getData (p,                    
            "uuid", id
        );
                
        final JsonObject d = p.getJsonObject ("data");
        
        final boolean isFileBeingUpdated = d.containsKey ("type");
                
        if (isFileBeingUpdated) {              // изменение существующего файла
                                        
            db.update (getTable (), HASH (         // обнуляем BLOB (см. триггер)
                "uuid", id,
                "len", 0
            ));
            
            data.put ("name", data.get ("label")); // перекидываем label в name
            data.remove ("label");
                    
            data.put ("mime", d.getString ("type"));
            data.put ("len",  d.getJsonNumber ("size").bigIntegerValueExact ());            
                    
        }

        db.update (getTable (), data);

        if (!isFileBeingUpdated) logAction (db, user, id, "update");

    });}
    
    @Override
    public void download (String id, OutputStream out) throws IOException, WebApplicationException {fetchData ((db, job) -> {
        db.getStream (getTable (), id, "body", out);
    });}

}