package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.Queue;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.InNsiGroup;
import ru.eludia.products.mosgis.db.model.incoming.InNsiItem;
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
    private ArrayBlockingQueue<Integer> waitingRegistryNumbers = new ArrayBlockingQueue <> (1000);
    
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

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            UUIDPublisher.publish (inNsiQueue, 
                (UUID) db.insertId (InNsiGroup.class, HASH (
                    "listgroup", group.getName ())));
            
        }
        catch (Exception ex) {
            throw new IllegalStateException (ex);
        }        
        
    }

    @Override
    public void importNsi () {
        checkEmptyOkei();
        importNsiGroup (VocNsiListGroup.i.NSI);
    }
        
    @Override
    public void importNsiItems (int registryNumber) {
        checkEmptyOkei();
        if (waitingRegistryNumbers.contains (registryNumber)) {
            logger.warning ("Reloading registryNumber=" + registryNumber + " is already to schedule, bypassing it");
            return;
        }
        
        logger.warning ("Scheduling registryNumber=" + registryNumber + "...");
        waitingRegistryNumbers.add (registryNumber);
        
    }
    
    @Schedule (second="*/4", minute="*", hour="*", persistent=false)
    public void checkQueue () {
        
        Integer registryNumber = waitingRegistryNumbers.poll ();
        
        if (registryNumber == null) {
//            logger.info ("Nothing to do");
            return;
        }
        else {
            importNsiItems (registryNumber, null);
            logger.info ("registryNumber=" + registryNumber + " is requested; " + waitingRegistryNumbers.size () + " left to go");
        }        
        
    }
    
    @Override
    public void importNsiItems (int registryNumber, Integer page) {

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            UUIDPublisher.publish (inNsiItemQueue, 
                (UUID) db.insertId (InNsiItem.class, HASH (
                    "registrynumber", registryNumber,
                    "page", page
                )));
            
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
    
}