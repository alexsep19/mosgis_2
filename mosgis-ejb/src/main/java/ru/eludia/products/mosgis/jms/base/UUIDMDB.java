package ru.eludia.products.mosgis.jms.base;

import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.UUID;
import java.util.logging.Level;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.ejb.ModelHolder;

public abstract class UUIDMDB<T extends Table> extends TextMDB {
    
    protected abstract void handleRecord (DB db, final UUID uuid, Map<String, Object> r) throws SQLException;    
    
    private final Class<Table> getTableClass () {
        return (Class<Table>) ((ParameterizedType)getClass ().getGenericSuperclass ()).getActualTypeArguments () [0];
    }
    
    protected final Table getTable () {
        return ModelHolder.getModel ().get (getTableClass ());
    }
    
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "*");
    }
    
    @Override
    protected final void onTextMessage (TextMessage message) throws SQLException, JMSException {
                        
        String txt = message.getText ();
            
        UUID uuid = null;
        
        try {
            uuid = UUID.fromString (txt);
        }
        catch (IllegalArgumentException ex) {
            logger.log (Level.SEVERE, "Not a UUID: '" + txt + '"', ex);
        }
            
        if (uuid == null) return;
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            Map<String, Object> r = null;
            
            try {
                r = db.getMap (get (uuid));
            }
            catch (SQLException e) {
                logger.log (Level.SEVERE, "Cannot fetch '" + uuid + '"', e);
                return;
            }
            
            if (r == null) {
                logger.log (Level.SEVERE, "Record not found: '" + uuid + '"');
                return;
            }
            
            try {
                logger.log (Level.INFO, r.toString ());
                handleRecord (db, uuid, r);
            }
            catch (SQLException e) {
                logger.log (Level.SEVERE, "Cannot handle '" + uuid + '"', e);
                return;
            }
            
        }
        
    }
    
}