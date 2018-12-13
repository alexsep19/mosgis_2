package ru.eludia.products.mosgis.jmx;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.voc.VocOksm;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ejb.ModelHolder;

@Startup
@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
//@DependsOn ("Conf")
public class Oksm implements OksmMBean {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (Oksm.class.getName ());

    @PostConstruct
    public void registerInJMX () {

        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=Oksm");
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
    public void importOksm () {

        try {

            Files.list (fs.getPath (Conf.get (VocSetting.i.PATH_OKSM))).forEach (path -> {

                File file = path.toFile ();
                if (file.isDirectory ()) return;
                String name = file.getName ();
                if (!name.endsWith (".json")) return;
                if (!name.startsWith ("data-")) return;

                try (
                    FileInputStream fis   = new FileInputStream (file);
                    InputStreamReader isr = new InputStreamReader (fis, "windows-1251");
                    JsonReader reader     = Json.createReader (isr);
                    DB db = ModelHolder.getModel ().getDb ();
                ) {

                    db.upsert(

                        VocOksm.class,

                        reader.readArray ().stream ()
                            .map (v -> ((JsonObject) v))
                            .filter (o -> o.containsKey ("CODE") && o.containsKey("ALFA2") && o.containsKey("FULLNAME"))
                            .map (o -> HASH (
                                "code",     o.getString ("CODE"),
                                "shortname", o.getString ("SHORTNAME"),
                                "fullname", o.getString ("FULLNAME"),
                                "alfa3", o.getString("ALFA3"),
                                "alfa2", o.getString("ALFA2")
                            ))
                            .collect (Collectors.toList ())
                    );

                    return;

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