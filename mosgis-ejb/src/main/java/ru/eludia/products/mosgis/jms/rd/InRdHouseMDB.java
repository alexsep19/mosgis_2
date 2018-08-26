package ru.eludia.products.mosgis.jms.rd;

import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import org.datacontract.schemas._2004._07.dmwsnewlife.OwnObjectService;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocRd1;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsRdClient;
import ru.eludia.products.mosgis.jms.base.TextMDB;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inRdHouseQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class InRdHouseMDB extends TextMDB {
    
    private static final Logger logger = Logger.getLogger (InRdHouseMDB.class.getName ());
    
    @EJB
    WsRdClient wsRdClient;
    
    private String getField (int id) {
        
        switch (id) {
            case 782:  return "unom";
            case 3163: return "id_vc_rd_1540";
            case 2463: return "id_vc_rd_1240";
            default:   return null;
        }    
        
    }

    @Override
    protected void onTextMessage (TextMessage message) throws SQLException, JMSException {
        
        String text = message.getText ();
        
        final String[] ids = text.split (",");
        
        final int length = ids.length;
        
        long start = System.currentTimeMillis ();
        
        logger.info ("Got text: '" + text + "'");
                
        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            try (DB.RecordBuffer buffer = db.new UpdateBuffer (VocRd1.class, 100)) {                

                buffer.setOnFlush (n -> {
                    logger.info (n + "/" + length + ": " + (1000 * n / (System.currentTimeMillis () - start)) + "r/s");
                });

                for (String id: ids) {

                    OwnObjectService o = wsRdClient.getObjectByObjectID (Integer.valueOf (id), 1);

                    Map<String, Object> r = HASH (
                        "id", id, 
                        "ts_get_object", NOW
                    );

                    o.getProperties ().getValue ().getObjectPropertyService ().forEach (i -> {
                        
                        final String fn = getField (i.getPropertyModelID ());
                                
                        if (fn == null) return;
                        
                        final String v = i.getDescription ().getValue ();
                        
                        if (v == null || v.isEmpty ()) return;
                        
                        r.put (fn, v);

                    });

                    buffer.add (r);

                };

            }
            catch (Exception ex) {
                logger.log (Level.SEVERE, null, ex);
            }
            
        }            
                        
    }    

}