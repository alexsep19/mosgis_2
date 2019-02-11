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
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.rest.api.AccountIndividualServicesLocal;
import ru.eludia.products.mosgis.db.model.tables.AccountIndividualService;
import ru.eludia.products.mosgis.db.model.tables.AdditionalService;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AccountIndividualServicesImpl extends BaseCRUD<AccountIndividualService> implements AccountIndividualServicesLocal  {

    private static final Logger logger = Logger.getLogger (AccountIndividualServicesImpl.class.getName ());    

    @Override
    public JsonObject doCreate (JsonObject p, User user) {return fetchData ((db, job) -> {

        JsonObject file = p.getJsonObject ("file");
                       
        db.begin ();        
        
            final Map<String, Object> h = ((AttachTable) getTable ()).HASH (file);
            
            Object id;
            
            if (file.containsKey ("uuid")) {
                id = file.getString ("uuid");
                h.put (AttachTable.c.BODY.lc (), null);
                db.update (getTable (), h);
                logAction (db, user, id, VocAction.i.UPDATE);
            }
            else {
                id = db.insertId (getTable (), h);
                logAction (db, user, id, VocAction.i.CREATE);
            }
            
            job.add ("id", id.toString ());
            
        db.commit ();
        
    });}
        
    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction (db -> {
        
        byte [] bytes = Base64.getDecoder ().decode (p.getString ("chunk"));
                
        Connection cn = db.getConnection ();
            
        final AccountIndividualService table = (AccountIndividualService) getTable ();

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

//        publishMessage (action, id_log);

    }
    
    @Override
    public JsonObject doDelete (String id, User user) {return doAction (db -> {
        
        JsonObject houseFile = db.getJsonObject (ModelHolder.getModel ().get (AccountIndividualService.class, id, "*"));
            
        db.update (AccountIndividualService.class, HASH (
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
        Map<String, Object> r = ((AttachTable) getTable ()).HASH (p.getJsonObject ("data"), "uuid", id);
        r.put ("uuid", id);
        db.update (AccountIndividualService.class, r);
        logAction (db, user, id, VocAction.i.UPDATE);
    });}

    @Override
    public JsonObject select (JsonObject p, User u) {return fetchData ((db, job) -> {

        Select select = ModelHolder.getModel ()
            .select  (getTable (), "*", "uuid AS id")
            .toOne   (AdditionalService.class, "AS svc", "*").on ()
            .where   (AccountIndividualService.c.UUID_ACCOUNT, p.getJsonObject ("data").getString ("uuid_account"))
            .where   ("id_status", VocFileStatus.i.LOADED.getId ())
            .orderBy ("svc.label")
            .orderBy (AccountIndividualService.c.BEGINDATE.lc ())
        ;

        db.addJsonArrays (job, select);

    });}

}