package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;

@Startup
@Singleton
public class House implements HouseMBean {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (House.class.getName ());
    
    @EJB
    protected UUIDPublisher UUIDPublisher;    

    @Resource (mappedName = "mosgis.inHouseDataQueue")
    private Queue inHouseDataQueue;
        
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
            UUIDPublisher.publish (inHouseDataQueue, fiasHouseGuid);
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException ("Invalid FIASHouseGUID passed: '" + fiasHouseGuid + "'");
        }
    }

    @Override
    public void importHouseData(String uuid, String orgPPAGuid) {
     
        if (uuid == null) throw new IllegalArgumentException ("Empty uuid passed");
        
        if (orgPPAGuid == null) throw new IllegalArgumentException ("Empty orgPPAGuid passed");

        JsonObjectBuilder jb = Json.createObjectBuilder ();
        JsonObject jo = jb.add("uuid", uuid).add("orgPPAGuid", orgPPAGuid).build();
        
        UUIDPublisher.publish (inHouseDataQueue, jo.toString());
    }

}