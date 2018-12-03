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
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.products.mosgis.db.model.tables.StuckCharters;
import ru.eludia.products.mosgis.db.model.tables.StuckContracts;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;

@Startup
@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class TTLWatch implements TTLWatchMBean {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static final Logger logger = Logger.getLogger (TTLWatch.class.getName ());
    
    @EJB
    protected UUIDPublisher UUIDPublisher;

    @Resource (mappedName = "mosgis.stuckContractsQueue")
    Queue stuckContractsQueue;    
    
    @Resource (mappedName = "mosgis.stuckChartersQueue")
    Queue stuckChartersQueue;    
        
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
                
                db.forEach (m.select (StuckContracts.class, StuckContracts.c.UUID.lc ()).limit (0, 128), (rs) -> {
                                        
                    UUIDPublisher.publish (stuckContractsQueue, DB.to.UUIDFromHex (rs.getString (2)));

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

    @Override
    @Schedule (hour = "*", minute = "*", second = "2", persistent = false)
    public void checkCharters () {
        
        Model m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
            
            db.begin ();
            
            try {
                
                db.getString (new QP ("SELECT id FROM tb_locks WHERE id=? FOR UPDATE NOWAIT", "stuck_charters"));
                
                db.forEach (m.select (StuckCharters.class, StuckCharters.c.UUID.lc ()).limit (0, 128), (rs) -> {
                                        
                    UUIDPublisher.publish (stuckChartersQueue, DB.to.UUIDFromHex (rs.getString (2)));

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