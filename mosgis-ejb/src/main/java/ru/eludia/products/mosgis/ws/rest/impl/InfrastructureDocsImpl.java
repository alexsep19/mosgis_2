package ru.eludia.products.mosgis.ws.rest.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.Infrastructure;
import ru.eludia.products.mosgis.db.model.tables.InfrastructureFile;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.InfrastructureDocsLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InfrastructureDocsImpl extends BaseCRUD<InfrastructureFile> implements InfrastructureDocsLocal  {

    private static final Logger logger = Logger.getLogger (InfrastructureDocsImpl.class.getName ());    

    private static final String PARENT_REF = InfrastructureFile.c.UUID_OKI.lc ();

    @Override
    public JsonObject doCreate (JsonObject p, User user) {return fetchData ((db, job) -> {

        JsonObject file = p.getJsonObject ("file");

        db.begin ();

            Object id = db.insertId (getTable (), HASH (
                PARENT_REF,       file.getString (PARENT_REF),
                "id_type",        file.getInt    ("id_type", 1),
                "label",          file.getString ("label"),
                "description",    file.getString ("description", ""),
                "mime",           file.getString ("type"),
                "len",            file.getInt    ("size")                
            ));

            job.add ("id", id.toString ());

            logAction (db, user, id, VocAction.i.CREATE);

        db.commit ();
        
    });}
        
    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction (db -> {
        
        byte [] bytes = Base64.getDecoder ().decode (p.getString ("chunk"));
                
        Connection cn = db.getConnection ();
            
        final InfrastructureFile table = (InfrastructureFile) getTable ();

        final String uuid = id.replace ("-", "").toUpperCase ();

        try (PreparedStatement st = cn.prepareStatement ("SELECT body, DBMS_LOB.GETLENGTH(body) FROM " + table.getName () + " WHERE uuid = ? FOR UPDATE")) {

            st.setString (1, uuid);

            try (ResultSet rs = st.executeQuery ()) {

                if (rs.next ()) {

                    Blob blob = rs.getBlob (1);
                    long  len = rs.getLong (2);

                    try (OutputStream os = blob.setBinaryStream (len + 1)) {
                        os.write (bytes);                            
                    }

                }

            }
                
            db.update (getTable (), HASH (
                "uuid",      uuid,
                "id_status", 0
            ));

        }                    

    });}
    
    @Override
    protected void logAction (DB db, User user, Object id, VocAction.i action) throws SQLException {

        Table logTable = ModelHolder.getModel ().getLogTable (getTable ());

        if (logTable == null) return;

        String id_log = db.insertId (logTable, HASH (
            "action", action,
            "uuid_object", id,
            "uuid_user", user == null ? null : user.getId ()
        )).toString ();
        
        db.update (getTable (), HASH (
            "uuid",      id,
            "id_log",    id_log
        ));

    }
    
    @Override
    public JsonObject doDelete (String id, User user) {return doAction (db -> {
        
        JsonObject houseFile = db.getJsonObject (ModelHolder.getModel ().get (InfrastructureFile.class, id, "*"));
            
        db.update (InfrastructureFile.class, HASH (
            "uuid",      id,
            "id_status", 2
        ));
        
        logAction (db, user, id, VocAction.i.DELETE);
        
    });}
    
    @Override
    public void download (String id, OutputStream out) throws IOException, WebApplicationException {fetchData ((db, job) -> {        
        db.getStream (getTable (), id, "body", out);
    });}

    @Override
    public JsonObject getItem (String id, User user) {

        try (DB db = ModelHolder.getModel ().getDb ()) {            
            return db.getJsonObject (ModelHolder.getModel ().get (getTable (), id, "*"));
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

    }

    @Override
    public JsonObject doEdit (String id, JsonObject p, User user) {return doAction (db -> {
        
        final JsonObject data = p.getJsonObject ("data");

        db.update (InfrastructureFile.class, HASH (
            "uuid",            id,
            "id_type",        data.getInt    ("id_type", 1),
            "description",     data.getString ("description",     "")
        ));
        
        logAction (db, user, id, VocAction.i.UPDATE);

    });}

    @Override
    public JsonObject select (JsonObject p, User u) {return fetchData ((db, job) -> {
        
        ComplexSearch s = new ComplexSearch (p.getJsonArray ("search"));
        
        if (!s.getFilters ().containsKey (PARENT_REF)) throw new IllegalStateException (PARENT_REF +" filter is not set");
                
        Select select = ModelHolder.getModel ()
            .select (getTable (), "*", "uuid AS id")
            .toMaybeOne (Infrastructure.class, "AS obj").on ()
            .where  ("id_status",  1);
        
        db.addJsonArrays (job, s.filter (select, ""));

    });}

}