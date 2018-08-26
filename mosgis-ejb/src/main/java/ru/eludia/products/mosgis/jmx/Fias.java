package ru.eludia.products.mosgis.jmx;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
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
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.incoming.InFias;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;

@Startup
@Singleton
public class Fias implements FiasMBean {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (Fias.class.getName ());
    
    @EJB
    protected UUIDPublisher UUIDPublisher;

    @Resource (mappedName = "mosgis.inFiasQueue")
    Queue inFiasQueue;
    
    @PostConstruct
    public void registerInJMX () {
        
        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=Fias");
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
    
    private static final FileSystem fs = FileSystems.getDefault ();
    
    private class Import {

        Map<String, Object> record = HASH ();
        
        Map<String, Path> n2p = new HashMap<> ();
        
        String [] names = new String [] {
            "strstat",
            "eststat",
            "addrobj",
            "house"
        };
        
        private void add (String name) {
            
            Path p = n2p.get (name);
            
            record.put ("sz_" + name, p.toFile ().length ());
            record.put ("uri_" + name, p.toUri ().toString ());
            
        }
                
        public Import () {
        
            try {
                
                Files.list (fs.getPath (Conf.get (VocSetting.i.PATH_FIAS))).forEach (path -> {

                    File file = path.toFile ();
                    if (file.isDirectory ()) return;
                    String name = file.getName ();
                    if (!name.endsWith (".XML")) return;
                    if (!name.startsWith ("AS_")) return;
                    if ( name.startsWith ("AS_DEL_")) return;
                    String[] part = name.split ("_");
                    n2p.put (part [1].toLowerCase (), path);
                    if (!record.isEmpty ()) return;
                    String dt = part [2];
                    record.put ("dt", dt.substring (0, 4) + '-' + dt.substring (4, 6) + '-' + dt.substring (6, 8));
                });
        
                for (String name: names) add (name);
                
            }
            catch (Exception ex) {
                throw new IllegalStateException (ex);
            }

        }               

        public Map<String, Object> getRecord () {
            return record;
        }        
        
    }

    @Override
    public void importFias () {
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
                                    
            UUIDPublisher.publish (inFiasQueue, (UUID) db.insertId (InFias.class, (new Import ()).getRecord ()));
            
        }
        catch (SQLException ex) {
            Logger.getLogger (Fias.class.getName()).log (Level.SEVERE, null, ex);
        }

    }

}