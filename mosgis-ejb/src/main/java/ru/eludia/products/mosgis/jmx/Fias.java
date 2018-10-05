package ru.eludia.products.mosgis.jmx;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
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
import ru.eludia.products.mosgis.db.model.incoming.InFias;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

@Startup
@Singleton
//@DependsOn ("Conf")
public class Fias implements FiasMBean {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (Fias.class.getName ());
    
    @EJB
    protected UUIDPublisher UUIDPublisher;

    @Resource (mappedName = "mosgis.inFiasQueue")
    Queue inFiasQueue;
    
    public enum Names {
        HOUSE,
        ADDROBJ,
        ESTSTAT,
        STRSTAT;
    };
    
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
        
        private void add (String name, FileHeader header) {
            
            record.put ("sz_" + name, header.getFullUnpackSize ());
            record.put ("uri_" + name, header.getFileNameW ());
            
        }
                
        public Import () {
            
            CheckPath ch = new CheckPath(fs.getPath (Conf.get (VocSetting.i.PATH_FIAS), "fias_xml.rar"));
            ch.check();
                
            try {
        
                for (Fias.Names name: Fias.Names.values()) {
                    
                    FileHeader header = ch.getHeader (name);
                    
                    if (!record.isEmpty ()) {
                        String headerName = header.getFileNameW ();
                        String[] part = headerName.split ("_");
                        String dt = part [2];
                        record.put ("dt", dt.substring (0, 4) + '-' + dt.substring (4, 6) + '-' + dt.substring (6, 8));
                        record.put ("uri_archive", Conf.get (VocSetting.i.PATH_FIAS) + File.separator + "fias_xml.rar");
                    }

                    add (name.toString ().toLowerCase (), header);
                    
                }
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
    public static class CheckPath {
        
        Map<Names, FileHeader> n2h = new HashMap<> ();
    
        public CheckPath (Path fiasRar) {
            
            try {
                
                File file = fiasRar.toFile ();
                FileInputStream fis = new FileInputStream (file);
                
                Archive archive = new Archive ();
                List<FileHeader> filesList = archive.readFileHeaders(fis);
                
                fis.close ();
                
                filesList.forEach (header -> {
                    for (Fias.Names c: Fias.Names.values())
                        if (header.getFileNameW ().endsWith (".XML") && header.getFileNameW ().startsWith ("AS_" + c.toString() + "_"))
                                 n2h.put (c, header);
                });
            }
            catch (Exception ex) {
                throw new IllegalStateException (ex);
            }
        }
        
        public void check() {
            
            for (Fias.Names c: Fias.Names.values())
                if (n2h.get(c) == null)
                    throw new IllegalStateException ("there is no file with the name : 'AS_" + c.toString() + "_'");
            
        }
        public FileHeader getHeader (Fias.Names s) {
            return n2h.get(s);
        }
        
    }
}