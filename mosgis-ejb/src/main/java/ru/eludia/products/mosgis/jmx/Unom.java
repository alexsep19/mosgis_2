package ru.eludia.products.mosgis.jmx;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.lang.management.ManagementFactory;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.db.model.voc.VocUnom;
import ru.eludia.products.mosgis.db.model.voc.VocUnomBuf;
import ru.eludia.products.mosgis.db.model.voc.VocUnom.c;
import ru.eludia.products.mosgis.db.model.voc.VocUnomStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;

@Startup
@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class Unom implements UnomMBean {
    
    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (Unom.class.getName ());
    
    @PostConstruct
    public void registerInJMX () {
        
        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=Unom");
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
    
    @Override
    public void importUnom () {
        
        try {
            
            Files.list (fs.getPath (Conf.get (VocSetting.i.PATH_UNOM))).forEach ((path) -> {
                
                File file = path.toFile ();
                if (file.isDirectory ()) return;
                String name = file.getName ();
                if (!name.toUpperCase().endsWith (".csv")) return;
                
                MosGisModel model = ModelHolder.getModel ();
                
                try (DB db = model.getDb ()) {
                    importFile (db, file);
                }
                catch (Exception e) {
                    logger.log (Level.SEVERE, "Cannot import UNOM", e);
                }                
                
            });
            
        }
        catch (Exception ex) {
            throw new IllegalStateException (ex);
        }
        
    }

    static void importFile (DB db, File file) throws Exception {
        
        try (FileInputStream is = new FileInputStream (file)) {
            
            try (InputStreamReader r = new InputStreamReader (is)) {
                
                try (BufferedReader br = new BufferedReader (r)) {
                    
                    br.readLine ();
                    
                    try (DB.RecordBuffer b = db.new TableUpsertBuffer (VocUnom.class, 100, VocUnomBuf.class, 1000)) {
                        
                        while (true) {

                            String line = br.readLine ();

                            if (line == null) break;

                            String[] parts = line.split (";");

                            Map<String, Object> h = DB.HASH (
                                c.UNOM, parts [0],
                                c.KLADR, parts [1],
                                c.FIAS, parts [2],
                                c.KAD_N, parts [3]
                            );

                            b.add (h);

                        }
                        
                    }
                                        
                }
                
            }
            
        }
        
        db.d0 ("UPDATE vc_unom SET id_status = " + VocUnomStatus.i.DUPLICATED_FIAS + " WHERE fiashouseguid IN (SELECT fiashouseguid FROM vc_unom WHERE fiashouseguid IS NOT NULL GROUP BY fiashouseguid HAVING COUNT(fiashouseguid) > 1)");
        
    }
    
}