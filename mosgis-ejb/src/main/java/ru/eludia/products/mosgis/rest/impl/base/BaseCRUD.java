package ru.eludia.products.mosgis.rest.impl.base;

import java.sql.SQLException;
import java.util.Map;
import javax.ejb.EJB;
import javax.jms.Queue;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

public abstract class BaseCRUD <T extends Table> extends Base<T> implements CRUDBackend {

    protected static final String UUID_ORG = "uuid_org";

    @EJB
    protected UUIDPublisher UUIDPublisher;

    protected Queue getQueue () {
        return null;
    }
    
    protected void publishMessage (String action, String id_log) {
        
        Queue queue = getQueue ();
        
        if (queue != null) UUIDPublisher.publish (queue, id_log);
        
    }

    protected void logAction (DB db, User user, Object id, String action) throws SQLException {

        Table logTable = ModelHolder.getModel ().getLogTable (getTable ());

        if (logTable == null) return;

        String id_log = db.insertId (logTable, HASH (
            "action", action,
            "uuid_object", id,
            "uuid_user", user == null ? null : user.getId ()
        )).toString ();
        
        db.update (getTable (), HASH (
            "uuid",      id,
            "id_status", VocAsyncEntityState.i.PENDING.getId (),
            "id_log",    id_log
        ));

        publishMessage (action, id_log);

    }

    @Override
    public JsonObject getLog (String id, JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Table logTable = ModelHolder.getModel ().getLogTable (getTable ());
        
        if (logTable == null) return;
        
        Select select = ModelHolder.getModel ().select (logTable, "AS log", "*", "uuid AS id")
            .and ("uuid_object", id)
            .toMaybeOne (VocUser.class, "label").on ()
            .toMaybeOne (OutSoap.class, "AS soap", "id_status", "is_failed", "ts", "ts_rp", "err_text").on ()
            .orderBy ("log.ts DESC")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        logTable.getColumns ().forEachEntry (0, (i) -> {
            final Col value = i.getValue ();
            if (!(value instanceof Ref)) return;
            Ref ref = (Ref) value;
            switch (i.getKey ()) {
                case "uuid_user":
                case "uuid_out_soap":
                    return;
                default:
                    final Table targetTable = ref.getTargetTable ();
                    if (!targetTable.getColumns ().containsKey ("label")) return;
                    final String name = ref.getName ();
                    StringBuilder sb = new StringBuilder ("AS ");
                    if (name.startsWith ("uuid_")) {
                        sb.append (name.substring (5));
                    }
                    else if (name.startsWith ("id_")) {
                        sb.append (name.substring (3));
                    }
                    else {
                        sb.append (name);
                    }                            
                    select.toMaybeOne (targetTable, sb.toString (), "label").on (ref.getName ());
            }
        });

        db.addJsonArrayCnt (job, select);
        
    });}

    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db) -> {

        final Table table = getTable ();

        Map<String, Object> data = getData (p);

        if (table.getColumn (UUID_ORG) != null && !data.containsKey (UUID_ORG)) data.put (UUID_ORG, user.getUuidOrg ());

        Object insertId = db.insertId (table, data);
        
        logAction (db, user, insertId, "create");

    });}

    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        db.update (getTable (), getData (p,
            "uuid", id
        ));
        
        logAction (db, user, id, "update");
                        
    });}

    @Override
    public JsonObject doDelete (String id, User user) {return doAction ((db) -> {
        
        db.update (getTable (), HASH (
            "uuid",        id,
            "is_deleted",  1
        ));
        
        logAction (db, user, id, "delete");
                
    });}

    @Override
    public JsonObject doUndelete (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (
            "uuid",        id,
            "is_deleted",  0
        ));
        
        logAction (db, user, id, "undelete");

    });}

}