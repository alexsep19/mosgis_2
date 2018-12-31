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
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jms.base.TextMDB;
import ru.eludia.products.mosgis.jmx.TTLWatch;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.stuckGisRequestsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class StuckGisRequestsMDB extends TextMDB {

    private MosGisModel m;
    
    @PostConstruct
    public void init () {
        m = ModelHolder.getModel ();
    }

    @Override
    protected void onTextMessage (TextMessage message) throws SQLException, JMSException {
                
        try {
            
            Item item = new Item (message.getText ());
            
            try (DB db = m.getDb ()) {

                Map<String, Object> r = item.getRecord (db);
                                
                logger.info ("Fetched record: " + DB.to.json (r));

                VocGisStatus.i status = VocGisStatus.i.forId (r.get ("id_status"));

                if (status.isInProgress ()) {
                    
                    Map<String, Object> values = getValues (db, item.uuid, r);
                    
                    db.update (item.entityTable, values);                

                    if (values.containsKey ("uuid_out_soap")) db.update (item.logTable, DB.HASH (
                        "uuid", r.get ("id_log"),
                        "id_ctr_status", VocGisStatus.i.FAILED_STATE.getId (),
                        "uuid_out_soap", values.get ("uuid_out_soap")
                    ));
                    
//                  m.createIdLog (db, item.entityTable, null, item.uuid, VocAction.i.EXPIRE);
                                        
                }
                else {
                    logger.warning ("This record is not expired, status=" + status.name ());
                    return;
                }                                
                
            }

        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot handle stuck GIS reguest", ex);
            return;
        }

    }
    
    private class Item {
        
        private static final int UUID_LEN = 36;
        
        UUID uuid;
        Table entityTable;
        Table logTable;
        Col statusCol;

        public Item (String s) throws Exception {
            
            uuid          = UUID.fromString (s.substring (0, UUID_LEN));
            
            final String tableName = s.substring (UUID_LEN);            
            entityTable  = m.get (tableName);            
            if (entityTable == null) throw new IllegalArgumentException ("Table not found: " + tableName);            

            logTable      = m.getLogTable (entityTable);
            if (logTable  == null) throw new IllegalArgumentException ("logTable not found for " + entityTable.getName ());

            statusCol     = TTLWatch.getStatusCol (entityTable);
            if (statusCol == null) throw new IllegalArgumentException ("statusCol not found for " + entityTable.getName ());
            
        }
        
        private Select getQuery () {
            
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
        
        Map<String, Object> getRecord (DB db) throws Exception {
            
            Map<String, Object> r = db.getMap (getQuery ());
            
            if (r == null) throw new javax.jms.IllegalStateException ("Record not found, bailing out");
            
            return r;
            
        }
        
    }

    Map<String, Object> getValues (DB db, UUID uuid, Map<String, Object> r) throws SQLException {            
        
        final Map<String, Object> values = HASH (
            "uuid", uuid,
            "id_ctr_status", VocGisStatus.i.FAILED_STATE.getId ()
        );
        
        UUID uuidOutSoap = (UUID) r.get ("uuid_out_soap");
        
        if (uuidOutSoap == null) {
            values.put ("uuid_out_soap", OutSoap.addExpired (db));
        }
        else {
            OutSoap.expire (db, uuidOutSoap);
        }
        
        return values;
        
    }    
       
}