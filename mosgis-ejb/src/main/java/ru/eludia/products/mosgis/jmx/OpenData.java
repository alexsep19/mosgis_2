package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.incoming.InOpenData;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;

@Startup
@Singleton
@DependsOn ("ModelHolder")
public class OpenData implements OpenDataMBean {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (OpenData.class.getName ());
    
    @EJB
    protected UUIDPublisher UUIDPublisher;

    @Resource (mappedName = "mosgis.inOpenDataQueue")
    Queue inOpenDataQueue;
    
    @PostConstruct
    public void registerInJMX () {
        /*
        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=OpenData");
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
    public void importOpenData () {
        
        try (DB db = ModelHolder.getModel ().getDb ()) {                                    
            UUIDPublisher.publish (inOpenDataQueue, (UUID) db.insertId (InOpenData.class, HASH ("dt_from", null)));            
        }
        catch (SQLException ex) {
            Logger.getLogger (OpenData.class.getName()).log (Level.SEVERE, null, ex);
        }

    }

}