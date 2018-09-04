package ru.eludia.products.mosgis.rest.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.JsonObject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.rest.api.ContractDocsLocal;
import ru.eludia.products.mosgis.db.model.tables.ContractFile;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
public class ContractDocsImpl extends BaseCRUD<ContractFile> implements ContractDocsLocal  {

    private static final Logger logger = Logger.getLogger (ContractDocsImpl.class.getName ());    

    @Override
    public JsonObject doCreate (JsonObject p, User user) {return fetchData ((db, job) -> {

        JsonObject file = p.getJsonObject ("file");
                       
        db.begin ();

            job.add ("id", 
                db.insertId (getTable (), HASH (
                    "uuid_contract",  file.getString ("uuid"),
                    "purchasenumber", file.getString ("purchasenumber", ""),
                    "id_type",        file.getInt    ("id_type"),
                    "label",          file.getString ("label"),
                    "description",    file.getString ("description", ""),
                    "mime",           file.getString ("type"),
                    "len",            file.getInt    ("size")
                )).toString ()
            );

        db.commit ();
        
    });}
        
    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction (db -> {
        
        byte [] bytes = Base64.getDecoder ().decode (p.getString ("chunk"));
                
        Connection cn = db.getConnection ();
            
        final ContractFile table = (ContractFile) getTable ();

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
    public JsonObject doDelete (String id, User user) {return doAction (db -> {
        
        JsonObject houseFile = db.getJsonObject (ModelHolder.getModel ().get (ContractFile.class, id, "*"));
            
        db.update (ContractFile.class, HASH (
            "uuid",      id,
            "id_status", 2
        ));
        
    });}
    
    @Override
    public void download (String id, OutputStream out) throws IOException, WebApplicationException {fetchData ((db, job) -> {        
        db.getStream (getTable (), id, "body", out);
    });}

    @Override
    public JsonObject getItem (String id) {

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

        db.update (ContractFile.class, HASH (
            "uuid",           id,
            "purchasenumber", data.getString ("purchasenumber", ""),
            "description",    data.getString ("description", "")
        ));

    });}

    @Override
    public JsonObject select (JsonObject p, User u) {return fetchData ((db, job) -> {

        JsonObject search = p.getJsonObject ("search");

        db.addJsonArrays (job, ModelHolder.getModel ()
            .select (getTable (), "*", "uuid AS id")
            .where ("uuid_contract", search.getString ("uuid_contract"))
            .and   ("id_status",   1)
        );

    });}

}