package ru.eludia.products.mosgis.rest.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.rest.api.InXlFilesLocal;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile.c;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFileLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.db.model.voc.VocXLFileType;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InXlFilesImpl extends BaseCRUD<InXlFile> implements InXlFilesLocal  {

    private static final Logger logger = Logger.getLogger (InXlFilesImpl.class.getName ());    
    
    @Resource (mappedName = "mosgis.inXlContractObjectsQueue")
    Queue inXlContractObjectsQueue;
    
    @Resource (mappedName = "mosgis.inXlHousesQueue")
    Queue inXlHousesQueue;

    public Queue getQueue (VocXLFileType.i type) {
        
        switch (type) {
            case CTR_OBJECTS:     return inXlContractObjectsQueue;
            case HOUSE_PASSPORTS: return inXlHousesQueue;
            default: throw new IllegalArgumentException ("XL file type not suported: " + type);
        }

    }    

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
            
            Map<String, Object> item = db.getMap (getTable (), id);
            
            if (VocFileStatus.i.forId (item.get (c.ID_STATUS.lc ())) == VocFileStatus.i.LOADED) {
                
                db.update (getTable (), HASH (
                    EnTable.c.UUID,      uuid,
                    c.ID_STATUS,         VocFileStatus.i.PROCESSING.getId ()
                ));
                
                UUIDPublisher.publish (
                    getQueue (VocXLFileType.i.forId (item.get (c.ID_TYPE.lc ()))), 
                    item.get ("uuid").toString ()
                );
                
            }

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
    public void download_errors (String id, OutputStream out) throws IOException, WebApplicationException {fetchData ((db, job) -> {
        db.getStream (getTable (), id, "errr", out);
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
    public JsonObject select (JsonObject p, User u) {return fetchData ((db, job) -> {
        
//        ComplexSearch s = new ComplexSearch (p.getJsonArray ("search"));
        
//        if (!s.getFilters ().containsKey ("uuid_charter")) throw new IllegalStateException ("uuid_charter filter is not set");
                
        Select select = ModelHolder.getModel ()
            .select (getTable (), "AS root", "*", "uuid AS id")
            .toOne (VocOrganization.class, "AS org", "label").on ()
            .toOne (VocUser.class, "AS u", "label").on ()
            .toOne (InXlFileLog.class, "AS log", "ts").on ()
            .orderBy ("root.ts DESC")
            .limit (p.getInt ("offset"), p.getInt ("limit"))        
        ;
        
        if (DB.ok (u.getUuidOrg ())) select.and (c.UUID_ORG, u.getUuidOrg ());

        db.addJsonArrayCnt (job, select);

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

//        publishMessage (action, id_log);

    }

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder job = Json.createObjectBuilder ();
        
        VocFileStatus.addTo (job);
        VocXLFileType.addTo (job);
        
        return job.build ();
        
    }

}