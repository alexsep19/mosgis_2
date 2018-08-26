package ru.eludia.products.mosgis.jms.rd;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import org.datacontract.schemas._2004._07.dmwsnewlife.OwnObjectService;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.voc.VocRd1;
import ru.eludia.products.mosgis.db.model.voc.VocRd1Buf;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsRdClient;
import ru.eludia.products.mosgis.jms.base.TextMDB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.ejb.ModelHolder;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inRdHousesQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class InRdHousesMDB extends TextMDB {

    private static final int STEP = 1000;
    
    private static final Logger logger = Logger.getLogger (InRdHousesMDB.class.getName ());

    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @EJB
    WsRdClient wsRdClient;

    @Resource (mappedName = "mosgis.inRdHouseQueue")
    Queue inRdHouseQueue;    
        
    @Override
    protected void onTextMessage (TextMessage message) throws SQLException, JMSException {

        logger.info ("Sending getObjectsByModel request...");

        final List<OwnObjectService> ownObjectService = wsRdClient.getObjectsByModel (1, false).getOwnObjectService ();

        logger.info ("... got " + ownObjectService.size () + " ids");

        try (DB db = ModelHolder.getModel ().getDb ()) {

            try (DB.RecordBuffer buffer = db.new TableUpsertBuffer (VocRd1.class, 100, VocRd1Buf.class, 1000, null)) {
                
                buffer.setOnFlush (n -> {
                    
                    final List <Map<String, Object>> records = buffer.getRecords ();
                    
                    if (records.isEmpty ()) return;
                    
                    StringBuffer sb = new StringBuffer (records.get (0).get ("id").toString ());
                    for (int i = 1; i < records.size (); i ++) {
                        sb.append (',');
                        sb.append (records.get (i).get ("id").toString ());
                    }
                    
                    UUIDPublisher.publish (inRdHouseQueue, sb.toString ());
                    
                });

                for (OwnObjectService i: ownObjectService) {
                    
                    final String name = i.getName ().getValue ();
                    
                    if (name == null) {
                        logger.severe ("No name set for id = " + i.getID ());
                        continue;
                    }
                    
                    final String address = name.replace ("Дом по адресу ", "");
                    
                    if (address.isEmpty ()) {
                        logger.severe ("Empty address for id = " + i.getID ());
                        continue;
                    }

                    buffer.add (HASH (
                        "id",      i.getID (),
                        "address", address)
                    );

                }
                                
            }

        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, null, ex);
        }

    }

}