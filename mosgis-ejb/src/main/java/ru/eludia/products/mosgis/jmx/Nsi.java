package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.Queue;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import static ru.eludia.products.mosgis.db.model.voc.VocNsiListGroup.i.NSI;
import static ru.eludia.products.mosgis.db.model.voc.VocNsiListGroup.i.NSIRAO;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.rest.ValidationException;

@Startup
@Singleton
@DependsOn ("Okei")
public class Nsi implements NsiMBean {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static final Logger logger = Logger.getLogger (Nsi.class.getName ());
    private ArrayBlockingQueue<Integer> waitingRegistryNumbers = new ArrayBlockingQueue <> (1000);
    
    @EJB
    protected UUIDPublisher UUIDPublisher;

    @Resource (mappedName = "mosgis.inNsiQueue")
    Queue inNsiQueue;
    
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
    public void importNsi () {
        checkEmptyOkei();
        UUIDPublisher.publish (inNsiQueue, String.valueOf (NSI.toString ()));
        UUIDPublisher.publish (inNsiQueue, String.valueOf (NSIRAO.toString ()));
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
    
    @Schedule (second="*/10", minute="*", hour="*", persistent=false)
    public void checkQueue () {
        
        Integer registryNumber = waitingRegistryNumbers.poll ();
        
        if (registryNumber == null) {
//            logger.info ("Nothing to do");
            return;
        }
        else {
            UUIDPublisher.publish (inNsiQueue, String.valueOf (registryNumber));
            logger.info ("registryNumber=" + registryNumber + " is requested; " + waitingRegistryNumbers.size () + " left to go");
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