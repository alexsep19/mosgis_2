package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingLog;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingUnknown;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.jms.UUIDPublisher;

@Startup
@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ImportHousesByFiasHouseGuid implements ImportHousesByFiasHouseGuidMBean {

    public static final VocSetting.i CONF_KEY = VocSetting.i.IS_IMPORTING_HOUSES;

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (ImportHousesByFiasHouseGuid.class.getName ());
    
    @EJB
    protected UUIDPublisher UUIDPublisher;    
    
    @EJB
    protected ConfLocal conf;    
   
/*
    @Resource (mappedName = "mosgis.inImportHousesByFiasHouseGuidQueue")
    Queue inImportHousesByFiasHouseGuidQueue;
*/    
    @PostConstruct
    public void registerInJMX () {
        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=ImportHousesByFiasHouseGuid");
            platformMBeanServer = ManagementFactory.getPlatformMBeanServer ();
            platformMBeanServer.registerMBean (this, objectName);
        } 
        catch (Exception e) {
            throw new IllegalStateException ("Problem during registration of Monitoring into JMX:" + e);
        }
    }

    @PreDestroy
    public void unregisterFromJMX () {
        try {
            platformMBeanServer.unregisterMBean (this.objectName);
        } catch (Exception e) {
            throw new IllegalStateException ("Problem during unregistration of Monitoring into JMX:" + e);
        }
    }
    
    private static boolean isInProgress () {
        return DB.ok (Conf.get (CONF_KEY));
    }

    @Override
    public String getState () {
        return isInProgress () ? "On" : "Off";
    }
    
    @Override
    public void start () {
        if (isInProgress ()) throw new IllegalStateException ("It was already started");
        conf.set (CONF_KEY, "1");
    }
    
    @Override
    public void stop () {
        if (!isInProgress ()) throw new IllegalStateException ("It was already stopped");
        conf.set (CONF_KEY, "0");
    }

    @Override
    public int getNumberOfFiasAddresses () {
        
        MosGisModel model = ModelHolder.getModel ();
        
        try (DB db = model.getDb ()) {
            return db.getCnt (model.select (VocBuilding.class, "*"));
        }
        catch (Exception e) {            
            logger.log (Level.SEVERE, "Can't fetch the number of FIAS addresses", e);
            return -1;
        }
        
    }
    
    @Override
    public int getNumberOfHouses () {
        
        MosGisModel model = ModelHolder.getModel ();
        
        try (DB db = model.getDb ()) {
            return db.getCnt (model.select (House.class, "*"));
        }
        catch (Exception e) {            
            logger.log (Level.SEVERE, "Can't fetch the number of house passports", e);
            return -1;
        }
        
    }
    
    @Schedule (hour = "*", minute = "*", second = "*/2", persistent = false)
    private void tick () {
        
        if (!isInProgress ()) return;
        
        logger.info ("tick...");
        
        MosGisModel model = ModelHolder.getModel ();
        
        try (DB db = model.getDb ()) {
            
            String fiashouseguid = db.getString (model.select (VocBuildingUnknown.class, VocBuildingUnknown.c.FIASHOUSEGUID.lc ()));
            
            if (!DB.ok (fiashouseguid)) {
                logger.info ("No more unknown GUIDs left, stopping import");
                stop ();
                return;
            }
                        
            String idLog = db.insertId (VocBuildingLog.class, HASH (
                "action",      VocAction.i.IMPORT_FROM_GIS_WS,
                "uuid_object", fiashouseguid
            )).toString ();
            
logger.info ("idLog=" + idLog);

        }            
        catch (Exception e) {            
            logger.log (Level.SEVERE, "Can't fetch the next FIASHOUSEGUID", e);
            return;
        }
                
    }

    @Override
    public int getNumberOfRequests () {
        
        MosGisModel model = ModelHolder.getModel ();
        
        try (DB db = model.getDb ()) {
            return db.getCnt (model.select (VocBuildingLog.class, "*").where ("uuid_out_soap", "IS NOT NULL"));
        }
        catch (Exception e) {            
            logger.log (Level.SEVERE, "Can't fetch the number of SOAP requests", e);
            return -1;
        }
    }
    
    @Override
    public int getNumberOfRequestsFailed () {
        
        MosGisModel model = ModelHolder.getModel ();
        
        try (DB db = model.getDb ()) {
            return db.getCnt (model
                .select (VocBuildingLog.class, "*")
                .toOne (OutSoap.class).where ("is_failed", 1).on ()
            );
        }
        catch (Exception e) {            
            logger.log (Level.SEVERE, "Can't fetch the number of failed SOAP requests", e);
            return -1;
        }
        
    }
    
}