package ru.eludia.products.mosgis.jms.base;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.base.db.sql.gen.Get;

public abstract class TransportRequestMDB<T extends Table> extends UUIDMDB<T> {
    
    protected static final String TRANSPORT_GUID = "TransportGUID";
    
    protected Logger logger = java.util.logging.Logger.getLogger (this.getClass ().getName ());
    
    @Override
    protected final Get get (UUID uuid) {
        return new Get (getTable (), uuid, "json");
    }

    @Override
    protected void handleRecord (DB db, final UUID uuid, Map<String, Object> r) throws SQLException {
        String json = (String) r.get ("json");
        splitBody (DB.to.JsonObject (json), uuid, db);
    }

    protected final void scanTransports (JsonArray transports, Map<String, BiConsumer<String, JsonObject>> handlers) {
        for (JsonValue i : transports) {
            JsonObject transport = (JsonObject) i;
            String transportGUID = transport.getString (TRANSPORT_GUID);
            for (Map.Entry<String, JsonValue> kv : transport.entrySet ()) {
                String key = kv.getKey ();
                if (TRANSPORT_GUID.equals (key)) continue;
                BiConsumer<String, JsonObject> handler = (BiConsumer<String, JsonObject>) handlers.get (key);
                if (handler == null)
                    logger.log (Level.SEVERE, "No handler for " + key);
                else
                    handler.accept (transportGUID, (JsonObject) kv.getValue ());
            }
        }
    }    
    
    protected abstract void splitBody (JsonObject body, final UUID uuid, DB db) throws SQLException;
    
}
