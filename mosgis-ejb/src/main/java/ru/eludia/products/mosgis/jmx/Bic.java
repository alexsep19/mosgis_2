package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.incoming.InVocBic;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.rest.User;

@Startup
@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class Bic implements BicMBean, BicLocal {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (Bic.class.getName ());
    
    @EJB
    protected UUIDPublisher UUIDPublisher;

    @Resource (mappedName = "mosgis.inBicQueue")
    Queue inBicQueue;
    
    @PostConstruct
    public void registerInJMX () {
        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=Bic");
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
    public void importBic (User user) {
        
        try (DB db = ModelHolder.getModel ().getDb ()) {                                    
            UUIDPublisher.publish (inBicQueue, (UUID) db.insertId (InVocBic.class, HASH (
                InVocBic.c.UUID_USER, user == null ?  null : user.getId ()
            )));
        }
        catch (Exception ex) {
            Logger.getLogger (Bic.class.getName()).log (Level.SEVERE, null, ex);
        }

    }

    @Override
    public void importBic () {
        importBic (null);
    }

}