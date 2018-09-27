package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.Queue;
import javax.json.JsonObject;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.nsi.InNsiGroup;
import ru.eludia.products.mosgis.db.model.incoming.nsi.InNsiItem;
import ru.eludia.products.mosgis.db.model.incoming.nsi.InNsiItemStatsOverview;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocNsiListGroup;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.rest.ValidationException;

@Startup
@Singleton
//@DependsOn ("Okei")
public class Nsi implements NsiMBean, NsiLocal {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static final Logger logger = Logger.getLogger (Nsi.class.getName ());
//    private ArrayBlockingQueue<Integer> waitingRegistryNumbers = new ArrayBlockingQueue <> (1000);
    
    @EJB
    protected UUIDPublisher UUIDPublisher;

    @Resource (mappedName = "mosgis.inNsiQueue")
    Queue inNsiQueue;
    
    @Resource (mappedName = "mosgis.inNsiItemQueue")
    Queue inNsiItemQueue;
    
    @PostConstruct
    public void registerInJMX () {
        
        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=Nsi");
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
    
    @Override
    public void importNsiGroup (VocNsiListGroup.i group) {
        
        UUID uuid = UUID.randomUUID ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            db.insert (InNsiGroup.class, HASH (
                "uuid", uuid,
                "listgroup", group.getName ()
            ));
            
            UUIDPublisher.publish (inNsiQueue, uuid);
                        
        }
        catch (Exception ex) {
            throw new IllegalStateException (ex);
        }        
        
    }

    @Override
    public void importNsi () {
        
        checkEmptyOkei ();
        
        importNsiGroup (VocNsiListGroup.i.NSI);
        
        importNsiGroup (VocNsiListGroup.i.NSIRAO);
        
    }
        
    @Override
    public void importNsiItems (int registryNumber) {
        
        checkEmptyOkei ();
        
        importNsiItems (registryNumber, null);
        
    }
    
    @Override
    public void checkForPending () {
        
        logger.info ("Checking for pending NSI item imports...");
        
        Model m = ModelHolder.getModel ();
                                
        try (DB db = m.getDb ()) {
            
            String sUID = db.getString (m.select (InNsiItem.class, "uuid").where ("uuid NOT IN", m.select (OutSoap.class, "uuid")));
            
            if (sUID != null) {
                
                UUID uuid = DB.to.UUIDFromHex (sUID);
                
                UUIDPublisher.publish (inNsiItemQueue, uuid);
                
                logger.info ("NSI import is launched for " + uuid);
                
            }
            else {
                
                logger.info ("NSI import is over");
                
            }

        }
        catch (Exception ex) {
            
            throw new IllegalStateException (ex);
            
        }                
        
    }
    
    @Override
    public void importNsiItems (int registryNumber, Integer page) {

        UUID uuid = UUID.randomUUID ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            db.insert (InNsiItem.class, HASH (
                "uuid",           uuid,
                "registrynumber", registryNumber,
                "page",           page
            ));

            UUIDPublisher.publish (inNsiItemQueue, uuid);

        }
        catch (Exception ex) {
            throw new IllegalStateException (ex);
        }        

    }    
    
    /**
     * При пустой vc_okei выбрасывает исключение.
     *
     * @throws ValidationException
     */
    private void checkEmptyOkei() throws ValidationException {
        final MosGisModel model = ModelHolder.getModel();
        try (DB db = model.getDb()) {
            if (db.getString(model.select(VocOkei.class, "code")) == null)
                throw new ValidationException("", "Перед импортом НСИ необходимо загрузить справочник ОКЕИ");
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Ошибка доступа к справочнику ОКЕИ");
        }
    }


    @Override
    public String getProgressStatusText () {
        
        JsonObject data = getProgressStatus ();

        logger.info ("data=" + data);
        
        if (data == null || data.isEmpty ()) return "No import in progress";
        
        int done    = data.getInt ("done", 0);
        int pending = data.getInt ("pending", 0);
        int total   = done + pending;
        
        final String started = data.getString ("started");

        StringBuilder sb = new StringBuilder ("Started at ");        
        sb.append (started);
        
        if (total > 0) {

            sb.append (' ');
            sb.append (done);
            sb.append ('/');
            sb.append (total);
            sb.append (" (");
            sb.append (100 * done / total);
            sb.append ("%) done.");

            if (done > 0) {
                
                final long now = System.currentTimeMillis () + 3 * 60 * 60 * 1000L;
                long elapsed = now - Timestamp.valueOf (started).getTime ();
                long remaining = elapsed * pending / done;

                sb.append (" Expected to complete by ");
                sb.append (new Timestamp (now + remaining));
                
            }            
            
        }
        
        sb.append ("...");
        
        return sb.toString ();
        
    }

    @Override
    public JsonObject getProgressStatus () {
        
        final MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {            
            return db.getJsonObject (m.select (InNsiItemStatsOverview.class, "*"));
        }
        catch (Exception e) {
            throw new IllegalStateException ("Can't get NSI import status", e);
        }        
        
    }
    
}