package ru.eludia.products.mosgis.rest.impl.base;

import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
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
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
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
    
    protected void publishMessage (VocAction.i action, String id_log) {
        
        Queue queue = getQueue ();
        
        if (queue != null) UUIDPublisher.publish (queue, id_log);
        
    }

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
            .orderBy ("log.ts DESC")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        if (logTable.getColumn ("uuid_out_soap") != null) select.toMaybeOne (OutSoap.class, "AS soap", "id_status", "is_failed", "ts", "ts_rp", "err_text", "uuid_ack").on ();
        
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
                    select.toMaybeOne (targetTable, "AS " + getSafeName (getNameByRef (name)), "label").on (ref.getName ());
                    
            }
        });

        db.addJsonArrayCnt (job, select);
        
    });}
    
    
    private String getSafeName (String name) {
        switch (name) {
            case "file":
                return name + '_';
            default:
                return name;
        }
    }
    
    private String getNameByRef (String name) {
        if (name.startsWith ("uuid_")) return name.substring (5);
        if (name.startsWith ("id_")) return name.substring (3);
        return name;
    }

    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db, job) -> {

        final Table table = getTable ();

        Map<String, Object> data = getData (p);

        if (table.getColumn (UUID_ORG) != null && !data.containsKey (UUID_ORG)) data.put (UUID_ORG, user.getUuidOrg ());

        Object insertId = db.insertId (table, data);
        
        job.add ("id", insertId.toString ());
        
        logAction (db, user, insertId, VocAction.i.CREATE);

    });}

    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        db.update (getTable (), getData (p,
            "uuid", id
        ));
        
        logAction (db, user, id, VocAction.i.UPDATE);
                        
    });}

    @Override
    public JsonObject doDelete (String id, User user) {return doAction ((db) -> {
        
        db.update (getTable (), HASH (
            "uuid",        id,
            "is_deleted",  1
        ));
        
        logAction (db, user, id, VocAction.i.DELETE);
                
    });}

    @Override
    public JsonObject doUndelete (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (
            "uuid",        id,
            "is_deleted",  0
        ));
        
        logAction (db, user, id, VocAction.i.UNDELETE);

    });}
    
    @Override
    public JsonObject getCach (String fiashouseguid) {return fetchData ((db, job) -> {
        
        VocBuilding.addCaCh(db, job, fiashouseguid);
        
    });}
    
    @Override
    public JsonObject getOktmo (String fiashouseguid) {return fetchData ((db, job) -> {
        
        JsonObject oktmo = db.getJsonObject(ModelHolder.getModel ()
                .get(VocBuilding.class, fiashouseguid, "oktmo")
        );
        
        if (oktmo != null) job.add ("oktmo", String.valueOf (oktmo.getInt("oktmo")));
        
    });}

}