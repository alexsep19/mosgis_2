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
import java.sql.Timestamp;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.fias.InFiasStatsOverview;

@Startup
@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
//@DependsOn ("Conf")
public class Fias implements FiasMBean, FiasLocal {

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
    
    @Override
    public String getProgressStatusText () {
        
        JsonObject data = getProgressStatus ();

        logger.info ("data=" + data);
        
        if (data == null || data.isEmpty () || 
           ((JsonNumber) data.get ("total_read")).longValue () == ((JsonNumber) data.get ("total_size")).longValue ()) {
            
            return "No import in progress";
        
        }
        
        long totalDone = ((JsonNumber) data.get ("total_read")).longValue ();
        long totalSize = ((JsonNumber) data.get ("total_size")).longValue ();
        
        long addrobjDone = ((JsonNumber) data.get ("addrobj_read")).longValue ();
        long addrobjSize = ((JsonNumber) data.get ("addrobj_size")).longValue ();
        
        long houseDone = ((JsonNumber) data.get ("house_read")).longValue ();
        long houseSize = ((JsonNumber) data.get ("house_size")).longValue ();
        
        long eststatDone = ((JsonNumber) data.get ("eststat_read")).longValue ();
        long eststatSize = ((JsonNumber) data.get ("eststat_size")).longValue ();
        
        long strstatDone = ((JsonNumber) data.get ("strstat_read")).longValue ();
        long strstatSize = ((JsonNumber) data.get ("strstat_size")).longValue ();
        
        final String started = data.getString ("started");

        StringBuilder sb = new StringBuilder ("Started at ");        
        sb.append (started);
        
        if (totalSize > 0) {

            sb.append ("| total: ");
            sb.append (totalDone);
            sb.append ('/');
            sb.append (totalSize);
            sb.append (" (");
            sb.append (100 * totalDone / totalSize);
            sb.append ("%) done ");
            
            sb.append ("| " + Fias.Names.HOUSE + ": ");
            sb.append (100 * houseDone / houseSize);
            sb.append ("% done ");
            
            sb.append ("| " + Fias.Names.ADDROBJ + ": ");
            sb.append (100 * addrobjDone / addrobjSize);
            sb.append ("% done ");
            
            sb.append ("| " + Fias.Names.ESTSTAT + ": ");
            sb.append (100 * eststatDone / eststatSize);
            sb.append ("% done ");

            sb.append ("| " + Fias.Names.STRSTAT + ": ");
            sb.append (100 * strstatDone / strstatSize);
            sb.append ("% done ");
            
            sb.append (" | ");
            
            if (totalSize > 0) {
                
                final long now = System.currentTimeMillis ();
                long elapsed = now - Timestamp.valueOf (started).getTime ();
                long remaining = elapsed * (totalSize - totalDone) / totalDone;

                sb.append (" Expected to complete by ");
                sb.append (new Timestamp (now + remaining));
                
            }            
            
        }
        
        sb.append ("...");
        
        return sb.toString ();
        
    }
    
    @Override
    public JsonObject getProgressStatus () {
        
        final MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {            
            return db.getJsonObject (m.select (InFiasStatsOverview.class, "*"));
        }
        catch (Exception e) {
            throw new IllegalStateException ("Can't get FIAS import status", e);
        }        
        
    }
}