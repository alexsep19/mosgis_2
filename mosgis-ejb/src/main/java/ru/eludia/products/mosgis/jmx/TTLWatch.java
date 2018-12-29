package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
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
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
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
    
    @Resource (mappedName = "mosgis.stuckGisRequestsQueue")
    Queue stuckGisRequestsQueue;        
        
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
    
    static final Object [] progressStatus = Arrays.asList (VocGisStatus.i.values ()).stream ()
        .filter ((t) -> t.isInProgress ())
        .map ((t) -> t.getId ())
        .toArray ();
        
    public static Ref getParentCol (Table t) {
        return (Ref) t.getColumn ("uuid_object");
    }
    
    public static Col getStatusCol (Table t) {
        
        Optional<String> getNameStatusGis = t.getColumns ().values ().stream ()
            .map (c -> c.getName ())
            .filter (n -> n.endsWith ("_status_gis"))
            .findFirst ();
        
        if (!getNameStatusGis.isPresent ()) return null;
        
        String nameStatusGis = getNameStatusGis.get ();
        
        return t.getColumn (nameStatusGis.substring (0, nameStatusGis.length () - 4));
        
    }
    
    void checkTables (DB db) throws Exception {        
        
        Model m = db.getModel ();
        
        int [] cnt = {1000};
        
        for (Table logTable: m.getTables ()) {
            
            if (cnt [0] <= 0) break;

            if (!logTable.getName ().endsWith ("__log")) continue;

            if (logTable.getColumn ("uuid_out_soap") == null) continue;

            Ref parentCol = getParentCol (logTable); if (parentCol == null) continue;

            Table entityTable = parentCol.getTargetTable ();

            Col statusCol = getStatusCol (entityTable); if (statusCol == null) continue;

            db.forEach (
                    
                m
                    .select (entityTable, "AS root", "uuid")
                    .where (statusCol.getName () + " IN", progressStatus)
                    .toOne  (logTable, "AS log")
                        .where ("ts <", new java.sql.Timestamp (System.currentTimeMillis () - 1000 * 60 * Conf.getInt (VocSetting.i.WS_GIS_ASYNC_TTL)))
                        .on ("root.id_log=log.uuid")
                    .limit (0, cnt [0])
                                        
                , (rs) -> {
                    
                    if (cnt [0] -- <= 0) return;

                    UUIDPublisher.publish (stuckGisRequestsQueue, DB.to.UUIDFromHex (rs.getString ("uuid")) + entityTable.getName ());
                    
            });
            
        }
        
    }
    
    @Override
    @Schedule (hour = "*", minute = "*", second = "0", persistent = false)
    public void checkTables () {
        
        Model m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
            
            db.begin ();
            
            try {
                
                db.getString (new QP ("SELECT id FROM tb_locks WHERE id=? FOR UPDATE NOWAIT", "stuck_contracts"));
                
                checkTables (db);
                                
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