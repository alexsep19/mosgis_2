package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.products.mosgis.db.model.tables.StuckContracts;
import ru.eludia.products.mosgis.ejb.ModelHolder;

@Startup
@Singleton
public class TTLWatch implements TTLWatchMBean {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static final Logger logger = Logger.getLogger (TTLWatch.class.getName ());
        
    @PostConstruct
    public void registerInJMX () {
        
        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=TTLWatch");
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
    @Schedule (hour = "*", minute = "*", second = "0", persistent = false)
    public void checkContracts () {
        
        Model m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
            
            db.begin ();
            
            try {
                
                db.getString (new QP ("SELECT id FROM tb_locks WHERE id=? FOR UPDATE NOWAIT", "stuck_contracts"));
                
                db.forEach (m.select (StuckContracts.class, StuckContracts.c.UUID.lc ()), (rs) -> {
                    
                    UUID uuid = DB.to.UUIDFromHex (rs.getString (1));
                    
                    logger.info ("Stuck contract detected: " + uuid);
                    
                });
                
            }
            catch (SQLException ex) {
                
                if (ex.getErrorCode () == 54) {
                    logger.info ("Can't acquire lock, skip operation");
                    return;
                }
                
                throw ex;
                
            }
            finally {
                db.commit ();
            }
            
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, null, ex);
        }
        
    }           

}