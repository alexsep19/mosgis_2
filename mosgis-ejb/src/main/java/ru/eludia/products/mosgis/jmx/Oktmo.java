package ru.eludia.products.mosgis.jmx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.UUID.randomUUID;
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
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ejb.ModelHolder;

@Startup
@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
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
        
        String areaCodeOfMoscow = "\"45\"";
        
        try {
            
            Files.list (fs.getPath (Conf.get (VocSetting.i.PATH_OKTMO))).forEach (path -> {
                
                File file = path.toFile ();
                if (file.isDirectory ()) return;
                String name = file.getName ();
                if (!name.toUpperCase().endsWith(".CSV")) return;
                if (!name.startsWith("data-")) return;
                
                try (
                    FileInputStream fis   = new FileInputStream (file);
                    BufferedReader br = new BufferedReader (new InputStreamReader(fis, "Cp1251"));
                    DB db = ModelHolder.getModel ().getDb ();
                ) {
                    
                    boolean inBlock = false;
                    String line;
                    List<Map<String, Object>> records = new ArrayList<>();
                    StringBuilder sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith(areaCodeOfMoscow)) {
                            
                            inBlock = true;
                            String[] parts = line.split (";");
                            for (int i = 0; i < parts.length; i++) {
                                parts[i] = parts[i].replace("\"", "");
                            }

                            HashMap<String, Object> map = new HashMap<>();
                            
                            sb.append(parts[1]);
                            sb.append(parts[2]);
                            sb.append(parts[3]);
                            sb.append(parts[5]);
                            
                            map.put (VocOktmo.c.KEY.lc (), sb.toString ());
                            
                            sb.delete(0, sb.length ());
                            
                            for (int i = 0; i < VocOktmo.fieldNames.length; i++)
                                map.put(VocOktmo.fieldNames[i], parts[i + 1]);
                            records.add(map);
                        }
                        else if (inBlock) break;
                        
                    }
                    
                    db.upsert (VocOktmo.class, records);
                    
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
