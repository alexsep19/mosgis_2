package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.util.UUID;
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
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.incoming.InAccessRequests;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jms.UUIDPublisher;

@Startup
@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
//@DependsOn ("Conf")
public class Delegation implements DelegationMBean, DelegationLocal {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (Delegation.class.getName ());
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @Resource (mappedName = "mosgis.inAccessRequestQueue")
    Queue inAccessRequestQueue;        
        
    @PostConstruct
    public void registerInJMX () {
        
        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=Delegation");
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
    @Schedule (hour = "04", minute = "02", second = "0", persistent = false)
    public void importAccessRequests () {
        importAccessRequests (null);
    }
    
    @Override
    public void importAccessRequests (Integer page) {

        Model model = ModelHolder.getModel ();
        
        try (DB db = model.getDb ()) {                        
            UUIDPublisher.publish (inAccessRequestQueue, (UUID) db.insertId (InAccessRequests.class, DB.HASH ("page", page)));
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Can't launch delegation rights import", ex);
        }

    }
            
}