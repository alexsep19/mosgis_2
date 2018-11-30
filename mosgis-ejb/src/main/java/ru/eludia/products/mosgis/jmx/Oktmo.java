package ru.eludia.products.mosgis.jmx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ejb.ModelHolder;

@Startup
@Singleton
public class Oktmo implements OktmoMBean {
    
    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (Okei.class.getName ());
    
    @PostConstruct
    public void registerInJMX () {
        
        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=Oktmo");
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
    public void importOktmo () {
        
        String areaCodeOfMoscow = "45";
        
        try {
            
            Files.list (fs.getPath (Conf.get (VocSetting.i.PATH_OKTMO))).forEach (path -> {
                
                File file = path.toFile ();
                if (file.isDirectory ()) return;
                String name = file.getName ();
                if (!name.toUpperCase().endsWith(".CSV")) return;
                if (!name.startsWith("data-")) return;
                
                try (
                    FileInputStream fis   = new FileInputStream (file);
                    BufferedReader br = new BufferedReader (new InputStreamReader(fis));
                    DB db = ModelHolder.getModel ().getDb ();
                ) {
                    
                    String record;
                    while ((record = br.readLine()) != null) {
                        
                        String[] parts = record.split (";");
                        for (String part : parts) {
                            part = part.replaceAll("^\"|\"$", "");
                            //if ("".equals(part)) part = null;
                        }
                        if (areaCodeOfMoscow.equals (parts[0])) {
                            
                            db.upsert (
                                    VocOktmo.class,
                                    
                                    db.HASH (
                                            VocOktmo.c.AREA_CODE.lc (),       parts[ 0],
                                            VocOktmo.c.SETTLEMENT_CODE.lc (), parts[ 1],
                                            VocOktmo.c.LOCALITY_CODE.lc (),   parts[ 2],
                                            VocOktmo.c.CONTROL_NUM.lc (),     parts[ 3],
                                            VocOktmo.c.SECTION_CODE.lc (),    parts[ 4],
                                            VocOktmo.c.SITE_NAME.lc (),       parts[ 5],
                                            VocOktmo.c.ADD_INFO.lc (),        parts[ 6],
                                            VocOktmo.c.DESCRIPTION.lc (),     parts[ 7],
                                            VocOktmo.c.AKT_NUM.lc (),         parts[ 8],
                                            VocOktmo.c.STATUS.lc (),          parts[ 9],
                                            VocOktmo.c.APPR_DATE.lc (),       parts[10],
                                            VocOktmo.c.ADOP_DATE.lc (),       parts[11]
                                    )
                            );
                            
                        }
                        
                    }
                    
                }
                catch (Exception ex) {
                    throw new IllegalStateException (ex);
                }
                
            });
            
        }
        catch (Exception ex) {
            throw new IllegalStateException (ex);
        }
        
    }
    
}
