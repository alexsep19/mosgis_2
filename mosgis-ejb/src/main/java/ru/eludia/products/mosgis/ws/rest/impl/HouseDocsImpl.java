package ru.eludia.products.mosgis.ws.rest.impl;

import ru.eludia.products.mosgis.ws.rest.impl.base.Base;
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
import ru.eludia.base.model.Ref;
import ru.eludia.products.mosgis.rest.api.HouseDocsLocal;
import ru.eludia.products.mosgis.db.model.tables.HouseFile;
import ru.eludia.products.mosgis.db.model.voc.VocPassportDocFields;
import ru.eludia.products.mosgis.db.ModelHolder;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class HouseDocsImpl extends Base<HouseFile> implements HouseDocsLocal  {

    private static final Logger logger = Logger.getLogger (HouseDocsImpl.class.getName ());
    
    private final void setDtNo (DB db, String uuid, String dt, String no, VocPassportDocFields.i field, String refName) throws SQLException {
        
        db.update (((Ref) ModelHolder.getModel ().get (HouseFile.class).getColumn (refName)).getTargetTable (), HASH (
            "uuid",                   uuid,
            "f_" + field.getId_dt (), dt,
            "f_" + field.getId_no (), no
        ));
        
    }

    @Override
    public JsonObject doCreate (JsonObject p) {return fetchData ((db, job) -> {

        JsonObject file = p.getJsonObject ("file");
        
        VocPassportDocFields.i field = VocPassportDocFields.i.forId (file.getString ("id_file_type"));        
        
        String refName = file.getString ("ref", field.getRefName ());
        
        db.begin ();
        
            setDtNo (db, 
                file.getString ("uuid"), 
                file.getString ("dt"), 
                file.getString ("no"), 
                field,
                refName
            );

            job.add ("id", 
                db.insertId (HouseFile.class, HASH (
                    refName,      file.getString ("uuid"),
                    "id_type",    field.getId (),
                    "label",      file.getString ("label"),
                    "note",       file.getString ("note"),
                    "mime",       file.getString ("type"),
                    "len",        file.getInt    ("size")
                )).toString ()
            );
                
        db.commit ();
        
    });}
        
    @Override
    public JsonObject doUpdate (String id, JsonObject p) {return doAction (db -> {
        
        byte [] bytes = Base64.getDecoder ().decode (p.getString ("chunk"));
                
        Connection cn = db.getConnection ();
            
        final HouseFile table = (HouseFile) getTable ();

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
                
            db.d0 (table.getStatusUpdateBuilder (uuid));

        }                    

    });}
    
    private static final String [] FFF = {"uuid_premise_res", "uuid_block", "uuid_living_room", "uuid_house"};
    
    private final String getRefName (JsonObject houseFile) {
        for (String i: FFF) if (houseFile.getString (i, null) != null) return i;
        throw new IllegalArgumentException ("Broken houseFile: " + houseFile);
    }

    @Override
    public JsonObject doDelete (String id) {return doAction (db -> {
        
        JsonObject houseFile = db.getJsonObject (ModelHolder.getModel ().get (HouseFile.class, id, "*"));
            
        VocPassportDocFields.i field = VocPassportDocFields.i.forId (houseFile.getString ("id_type"));        
                        
        setDtNo (db, 
            houseFile.getString (field.getRefName ()), 
            null, 
            null, 
            field,
            getRefName (houseFile)
        );

        db.update (HouseFile.class, HASH (
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
            return db.getJsonObject (ModelHolder.getModel ().get (HouseFile.class, id, "label", "len"));                        
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

    }

    @Override
    public JsonObject doEdit (String id, JsonObject p) {return doAction (db -> {
        
        JsonObject data = p.getJsonObject ("data");
        
        JsonObject houseFile = db.getJsonObject (ModelHolder.getModel ().get (HouseFile.class, id, "*"));

        VocPassportDocFields.i field = VocPassportDocFields.i.forId (houseFile.getString ("id_type"));        

        setDtNo (db, 
            houseFile.getString (field.getRefName ()), 
            data.getString ("dt"), 
            data.getString ("no"), 
            field,
            getRefName (houseFile)
        );

        db.update (HouseFile.class, HASH (
            "uuid", id,
            "note", data.getString ("note")
        ));
        
    });}

    @Override
    public JsonObject select (JsonObject p) {return fetchData ((db, job) -> {
        
        JsonObject search = p.getJsonObject ("search");

        db.addJsonArrays (job, ModelHolder.getModel ()
            .select (HouseFile.class, "uuid AS id", "id_type", "label", "len", "note")
            .where ("uuid_house", search.getString ("uuid_house", null))
            .and   ("id_status",   1)
        );
        
    });}

}