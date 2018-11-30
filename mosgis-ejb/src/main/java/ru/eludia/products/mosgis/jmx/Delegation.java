package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.Queue;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.incoming.InAccessRequests;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;

@Startup
@Singleton
//@DependsOn ("Conf")
public class Delegation implements DelegationMBean {

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
    public void importAccessRequests () {

        Model model = ModelHolder.getModel ();
        
        try (DB db = model.getDb ()) {                        
            UUIDPublisher.publish (inAccessRequestQueue, (UUID) db.insertId (InAccessRequests.class, DB.HASH ("page", null)));
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Can't launch delegation rights import", ex);
        }

    }
            
}