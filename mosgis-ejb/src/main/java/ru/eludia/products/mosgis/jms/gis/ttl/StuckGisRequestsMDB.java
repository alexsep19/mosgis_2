package ru.eludia.products.mosgis.jms.gis.ttl;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jms.base.TextMDB;
import ru.eludia.products.mosgis.jmx.TTLWatch;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.stuckGisRequestsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class StuckGisRequestsMDB extends TextMDB {

    private static final int UUID_LEN = 36;
    private MosGisModel m;
    
    @PostConstruct
    public void init () {
        m = ModelHolder.getModel ();
    }
    
    

    @Override
    protected void onTextMessage (TextMessage message) throws SQLException, JMSException {
                
        try {
            
            Select q = getQuery (message.getText ());

            try (DB db = m.getDb ()) {

                Map<String, Object> r = db.getMap (q);

logger.info ("r=" + DB.to.json (r));
                
            }

        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot handle stuck GIS reguest", ex);
            return;
        }

    }

    private Select getQuery (final String s) throws IllegalArgumentException {
        
        UUID uuid         = getUUID  (s);
        Table entityTable = getTable (s);
        
        Table logTable    = m.getLogTable (entityTable);
        if (logTable  == null) throw new IllegalArgumentException ("logTable not found for " + entityTable.getName ());
        
        Col statusCol     = TTLWatch.getStatusCol (entityTable);
        if (statusCol == null) throw new IllegalArgumentException ("statusCol not found for " + entityTable.getName ());
        
        return m
            .select (entityTable, "AS root"
                , "uuid"
                , "id_log"
                , statusCol.getName () + " AS id_status")
            .toOne  (logTable, "AS log"
                , "uuid_out_soap AS uuid_out_soap")
            .on ("root.id_log=log.uuid")
            .where ("uuid", uuid);
        
    }
    
/*
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws Exception {
        
        final Map<String, Object> values = getValues (db, uuid, r);
        
        db.update (Contract.class, values);                
        
        if (values.containsKey ("uuid_out_soap")) db.update (ContractLog.class, DB.HASH (
            "uuid", r.get ("id_log"),
            "id_ctr_status", VocGisStatus.i.FAILED_STATE.getId (),
            "uuid_out_soap", values.get ("uuid_out_soap")
        ));
        
    }
*/   

    private UUID getUUID (final String s) {
        return UUID.fromString (s.substring (0, UUID_LEN));
    }
    
    private Table getTable (final String s) {        
        final String tableName = s.substring (UUID_LEN);            
        final Table t = m.get (tableName);            
        if (t == null) throw new IllegalArgumentException ("Table not found: " + tableName);            
        return t;            
    }
       
}