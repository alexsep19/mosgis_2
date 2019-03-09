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
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.model.incoming.InLicenses;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jms.UUIDPublisher;

@Startup
@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
//@DependsOn ("Conf")
public class License implements LicenseMBean {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (License.class.getName ());
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @Resource (mappedName = "mosgis.inExportLicenseQueue")
    private Queue queue;        
        
    @PostConstruct
    public void registerInJMX () {
        
        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=License");
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
    @Schedule(hour = "1", persistent = false)
    public void importLicenses() {
        Model model = ModelHolder.getModel();

        try (DB db = model.getDb()) {
            db.forEach(model
                    .select(VocOrganization.class, "AS root", "uuid")
                    .toOne(VocOrganizationNsi20.class, "AS role",  "uuid").on("root.uuid = role.uuid AND role.is_deleted = 0 AND role.code = '1'")
                    .where("ogrn IS NOT NULL")
                    .and("is_deleted", 0)
                    , rs -> {
                UUIDPublisher.publish(queue, (UUID) db.insertId(InLicenses.class, DB.HASH("uuid_org", TypeConverter.UUID(rs.getBytes("uuid")))));
            });
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Can't launch licenses import", ex);
        }
    }
            
}