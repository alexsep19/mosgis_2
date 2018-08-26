package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
public class Org implements OrgMBean {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (Org.class.getName ());
    
    @EJB
    protected UUIDPublisher UUIDPublisher;    

    @Resource (mappedName = "mosgis.inOrgQueue")
    Queue inOrgQueue;        
        
    @PostConstruct
    public void registerInJMX () {
        
        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=Org");
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
    
    private static final Pattern RE = Pattern.compile ("\\d{13}(\\d\\d)?");    
            
    @Override
    public void importOrg (String ogrn) {
        
        if (ogrn == null) throw new IllegalArgumentException ("Empty ORGN passed");
        
        switch (ogrn.length ()) {
            case 13: case 15: break;
            default: throw new IllegalArgumentException ("ORGN must be either 13 or 15 chars long");
        }
        
        if (!RE.matcher (ogrn).matches ()) throw new IllegalArgumentException ("Invalid OGRN passed: '" + ogrn + "'");
        
        UUIDPublisher.publish (inOrgQueue, ogrn);
    
    }

}