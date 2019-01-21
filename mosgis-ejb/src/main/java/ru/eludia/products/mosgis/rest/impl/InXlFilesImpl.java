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
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.rest.api.InXlFilesLocal;
import ru.eludia.products.mosgis.db.model.incoming.InXlFile;
import ru.eludia.products.mosgis.db.model.incoming.InXlFile.c;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InXlFilesImpl extends BaseCRUD<InXlFile> implements InXlFilesLocal  {

    private static final Logger logger = Logger.getLogger (InXlFilesImpl.class.getName ());    

    @Override
    public JsonObject doCreate (JsonObject p, User user) {return fetchData ((db, job) -> {

        JsonObject file = p.getJsonObject ("file");
                       
        db.begin ();
        
            Object id = db.insertId (getTable (), HASH (
                c.UUID_USER,      user.getId (),
                c.ID_TYPE,        file.getInt    ("id_type"),
                c.LABEL,          file.getString ("label"),
                c.MIME,           file.getString ("type"),
                c.LEN,            file.getInt    ("size")                
            ));
        
            job.add ("id", id.toString ());

            logAction (db, user, id, VocAction.i.CREATE);
            
        db.commit ();
        
    });}
        
    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction (db -> {
        
        byte [] bytes = Base64.getDecoder ().decode (p.getString ("chunk"));
                
        Connection cn = db.getConnection ();
            
        final InXlFile table = (InXlFile) getTable ();

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
                EnTable.c.UUID,      uuid,
                c.ID_STATUS,         VocFileStatus.i.LOADING.getId ()
            ));

        }                    

    });}
    
    @Override
    public JsonObject doDelete (String id, User user) {return doAction (db -> {
        
        JsonObject houseFile = db.getJsonObject (ModelHolder.getModel ().get (InXlFile.class, id, "*"));
            
        db.update (InXlFile.class, HASH (
            EnTable.c.UUID,      id,
            c.ID_STATUS,         VocFileStatus.i.DELETED.getId ()
        ));
        
        logAction (db, user, id, VocAction.i.DELETE);
        
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
    public JsonObject select (JsonObject p, User u) {return fetchData ((db, job) -> {
        
        ComplexSearch s = new ComplexSearch (p.getJsonArray ("search"));
        
        if (!s.getFilters ().containsKey ("uuid_charter")) throw new IllegalStateException ("uuid_charter filter is not set");
                
        Select select = ModelHolder.getModel ()
            .select (getTable (), "*", "uuid AS id")
            .toMaybeOne (CharterObject.class, "AS obj").on ()
            .toMaybeOne (VocBuildingAddress.class, "AS fias", "label").on ("obj.fiashouseguid=fias.houseguid")
            .where  ("id_status",  1);
        
        db.addJsonArrays (job, s.filter (select, ""));

    });}

}