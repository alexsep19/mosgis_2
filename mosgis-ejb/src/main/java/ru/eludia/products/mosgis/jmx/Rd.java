package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.Queue;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.datacontract.schemas._2004._07.dmwsnewlife.ArrayOfOwnObjectModelService;
import org.datacontract.schemas._2004._07.dmwsnewlife.ObjectPropertyModelService;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.products.mosgis.db.model.rd.RdTable;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.voc.VocRd1House;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;
import ru.eludia.products.mosgis.db.model.voc.VocRdCol;
import ru.eludia.products.mosgis.db.model.voc.VocRdList;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsRdClient;

@Startup
@Singleton
//@DependsOn ("ModelHolder")
public class Rd implements RdMBean {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static final Logger logger = Logger.getLogger (Rd.class.getName ());
    
    @EJB
    WsRdClient wsRdClient;

    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @Resource (mappedName = "mosgis.inRdHousesQueue")
    Queue inRdHousesQueue;    
    
    @Resource (mappedName = "mosgis.inRdHouseQueue")
    Queue inRdHouseQueue;    
    
    @PostConstruct
    public void registerInJMX () {
        /*
        try {           
            
//            service.setHandlerResolver (new HandlerResolver () {
//                @Override
//                public List<Handler> getHandlerChain (PortInfo portInfo) {
//                    return Collections.singletonList (new LoggingOutMessageHandler ());
//                }
//            });
            
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=Rd");
            platformMBeanServer = ManagementFactory.getPlatformMBeanServer ();
            platformMBeanServer.registerMBean (this, objectName);
        } 
        catch (Exception e) {
            throw new IllegalStateException ("Problem during registration of Monitoring into JMX:" + e);
        }
        */
    }

    @PreDestroy
    public void unregisterFromJMX () {
        /*
        try {
            platformMBeanServer.unregisterMBean (this.objectName);
        } catch (Exception e) {
            throw new IllegalStateException ("Problem during unregistration of Monitoring into JMX:" + e);
        }
        */
    }

    @Override
    public void importRdModel (int modelId) {
                        
        try (DB db = ModelHolder.getModel ().getDb ()) {

            for (ObjectPropertyModelService i: wsRdClient.getModelProperties (modelId).getObjectPropertyModelService ()) {

                VocRdColType.i type = VocRdColType.i.forId (i.getPropertyValueType ());                
                
                db.upsert (VocRdCol.class, HASH (
                    "id",                   i.getID (),
                    "object_model_id",      i.getObjectModelID (),                    
                    "property_value_type",  i.getPropertyValueType (),
                    "link_dictionary",      i.getLinkDictionary ().getValue (),
                    "name",                 i.getName ().getValue ()
                ));
                
                if (type == VocRdColType.i.REF) importRdIdNames (i.getLinkDictionary ().getValue ().intValue ());

            }

        }
        catch (SQLException ex) {
            logger.log (Level.SEVERE, null, ex);
        }
        
    }
    
    private void add (List<Map<String, Object>> records, ArrayOfOwnObjectModelService models) {
        
        if (models == null) return;
        
        models.getOwnObjectModelService ().forEach (i -> {
            
            records.add (HASH (
                "parent",  i.getParentID ().getValue (),
                "modelid", i.getID (),
                "name",    i.getName ().getValue ()
            ));            
            
            add (records, i.getChilds ().getValue ());
                        
        });
                
    }

    @Override
    public void importRdModelsTree () {

        try (DB db = ModelHolder.getModel ().getDb ()) {
            List<Map<String, Object>> records = new ArrayList <> ();
            add (records, wsRdClient.getAllModelsTreeByRootId (1));
            db.upsert (VocRdList.class, records, null);
            
            records.forEach (i -> {importRdModel (Integer.valueOf (i.get ("modelid").toString ()));});
            
        }
        catch (SQLException ex) {
            logger.log (Level.SEVERE, null, ex);
        }

    }

    @Override
    public void importRdIdNames (int modelId) {

        try (DB db = ModelHolder.getModel ().getDb ()) {

            RdTable rdTable = new RdTable (db, modelId);
            
            db.updateSchema (rdTable);
            
            db.upsert (rdTable,
                    
                wsRdClient.getObjectsByModel (modelId, false).getOwnObjectService ().stream ().map (i -> HASH (
                    "id",   i.getID (),
                    "name", i.getName ().getValue ()
                )).collect (Collectors.toList ())
                    
            , null);
            
        }
        catch (SQLException ex) {
            logger.log (Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public void importRdHouses () {
        
        UUIDPublisher.publish (inRdHousesQueue, "");

    }

    @Override
    public void importRdLostHouses () {
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            StringBuilder sb = new StringBuilder ();
            
            db.forEach (new QP ("SELECT id FROM vc_rd_1 WHERE ts_get_object IS NULL"), rs -> {
                
                if (sb.length () > 0) sb.append (',');
                
                sb.append (rs.getInt (1));
                
            });
            
            UUIDPublisher.publish (inRdHouseQueue, sb.toString ());
            
        }
        catch (SQLException ex) {
            logger.log (Level.SEVERE, null, ex);
        }                

    }

    @Override
    public void importRdObjToHouses () {

        String [] cols = new String [] {"address", "fiashouseguid", "is_condo"};

        QP qp = new QP ("MERGE INTO ");
        qp.append (ModelHolder.getModel ().getName (House.class));
        qp.append (" d USING (SELECT * FROM ");
        qp.append (ModelHolder.getModel ().getName (VocRd1House.class));
        qp.append (") s ON (d.unom = s.unom) ");
        qp.append ("WHEN MATCHED THEN UPDATE SET ");
        for (String i: cols) {
            qp.append ("d.");
            qp.append (i);
            qp.append ("=s.");
            qp.append (i);
            qp.append (',');
        }
        qp.setLastChar (' ');
        qp.append ("WHEN NOT MATCHED THEN INSERT (unom");
        for (String i: cols) {
            qp.append (',');
            qp.append (i);
        }
        qp.append (") VALUES (s.unom");
        for (String i: cols) {
            qp.append (",s.");
            qp.append (i);
        }
        qp.append (')');

        try (DB db = ModelHolder.getModel ().getDb ()) {                                                
            db.d0 (qp);
        }
        catch (SQLException ex) {
            logger.log (Level.SEVERE, null, ex);
        }                

    }

}