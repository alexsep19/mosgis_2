package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.util.UUID;
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
import ru.eludia.products.mosgis.ejb.UUIDPublisher;

@Startup
@Singleton
public class ExportHouse implements ExportHouseMBean {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (ExportHouse.class.getName ());
    
    @EJB
    protected UUIDPublisher UUIDPublisher;    

    @Resource (mappedName = "mosgis.inExportHouseQueue")
    Queue inExportHouseQueue;        
        
    @PostConstruct
    public void registerInJMX () {
        
        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=ExportHouse");
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
    public void exportHouseData (String fiasHouseGuid) {
        
        if (fiasHouseGuid == null) throw new IllegalArgumentException ("Empty FIASHouseGUID passed");
        
        try {
            UUID.fromString(fiasHouseGuid);
            UUIDPublisher.publish (inExportHouseQueue, fiasHouseGuid);
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException ("Invalid FIASHouseGUID passed: '" + fiasHouseGuid + "'");
        }
    }

}