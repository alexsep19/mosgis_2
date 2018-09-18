package ru.eludia.products.mosgis.jms.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.lang.reflect.ParameterizedType;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.util.ProgressInputStream;
import ru.eludia.products.mosgis.db.model.incoming.InFias;
import ru.eludia.products.mosgis.db.model.incoming.InFiasVocBuilding;
import ru.eludia.products.mosgis.db.model.incoming.InFiasVocStreet;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocStreet;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingEstate;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingStructure;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import com.github.junrar.Archive;
import java.math.BigDecimal;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inFiasQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class InFiasMDB extends UUIDMDB<InFias> {
    
    SAXParserFactory spf = SAXParserFactory.newInstance ();
    private static final int PROGRESS_STEPS = 100;
    private static final int PACK_SIZE = 100;
    private static final int TB_SIZE = 1000;
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "*");        
    }    

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {

        Set<String> aoGuids = new HashSet ();

        db.update (InFias.class, HASH ("uuid", uuid, "dt_from", NOW));

        new BuildingEstateScanner    (r, db, uuid, aoGuids).run ();
        new BuildingStructureScanner (r, db, uuid, aoGuids).run ();
        new StreetScanner            (r, db, uuid, aoGuids).run ();
        new BuildingScanner          (r, db, uuid, aoGuids).run ();

        db.update (InFias.class, HASH ("uuid", uuid, "dt_to_fact", NOW));

    }

    private abstract class BScanner<T extends Table, TB extends Table> extends Scanner {
        
        Table tb;

        public BScanner (Map r, DB db, UUID uuid, Set aoGuids) {
            super (r, db, uuid, aoGuids);
            tb = ModelHolder.getModel ().get (getTableClass (1));
        }

        @Override
        DB.RecordBuffer createBuffer () throws SQLException {
            return db.new TableUpsertBuffer (t, PACK_SIZE, tb, TB_SIZE, null);
        }
        
    }
    
    private abstract class Scanner<T extends Table> extends DefaultHandler {
        
        Map<String, Object> r; 
        DB db; 
        UUID uuid;
        Set<String> aoGuids;

        Table t;
        DB.RecordBuffer b;
        List<String> fields;
        String tagName;
        String postfix;
        
        Scanner (Map<String, Object> r, DB db, UUID uuid, Set<String> aoGuids) {
            this.r = r;
            this.db = db;
            this.uuid = uuid;
            this.aoGuids = aoGuids;
            t = ModelHolder.getModel ().get (getTableClass (0));
            fields = new ArrayList ();
            for (Col col: t.getColumns ().values ()) {
                String s = col.getName ();
                if ("livestatus".equals (s)) continue;
                if (s.contains ("_")) continue;
                fields.add (s.toUpperCase ());
            }
        }
        
        final Class<Table> getTableClass (int n) {
            return (Class<Table>) ((ParameterizedType)getClass ().getGenericSuperclass ()).getActualTypeArguments () [n];
        }

        void extra (Attributes attributes) {}
        
        boolean isOff (Attributes attributes) {
            return false;
        }

        @Override
        public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if (!tagName.equals (qName) || isOff (attributes)) return;            
            
            Map <String, Object> r = HASH (
                "uuid_in_fias", uuid,
                "livestatus", 1
            );
            
            extra (attributes);
                        
            for (String i: fields) r.put (i.toLowerCase (), attributes.getValue (i));    

            try {
                b.add (r);
            }
            catch (SQLException ex) {
                throw new SAXException (ex);
            }
            
        }   
        
        DB.RecordBuffer createBuffer () throws SQLException {
            return db.new UpsertBuffer (t, PACK_SIZE, null);
        }
        
        public void run () {

            String fileUri = (String) r.get ("uri_" + postfix);
            String archiveUri = (String) r.get ("uri_archive");

            logger.log (Level.INFO, "Import file " + fileUri + " from archive " + archiveUri);

            try (DB.RecordBuffer bb = createBuffer ()) {
                
                b = bb;
                
                final String progressSQL = "UPDATE " + getTable ().getName () + " SET rd_" + postfix + "=? WHERE uuid=?";

                FileSystem fs = FileSystems.getDefault ();
                Path archivePath = fs.getPath(archiveUri);
                File archiveFile = archivePath.toFile ();
                Archive archive = new Archive ();
                
                try (FileInputStream fis = new FileInputStream (archiveFile)) {
                    try (PipedOutputStream pipeOS = new PipedOutputStream ()) {
                        try (PipedInputStream pipeIS = new PipedInputStream ()) {
                            pipeOS.connect(pipeIS);

                            ExecutorService executor = Executors.newSingleThreadExecutor ();
                            executor.execute(() -> {
                                try {
                                    archive.extractFile(fis, fileUri, pipeOS);
                                    pipeOS.close();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            final Long size = ((BigDecimal) r.get ("sz_" + postfix)).longValue ();
                            logger.log (Level.INFO, "SIZE " + size);
                            try (ProgressInputStream pis = new ProgressInputStream (pipeIS, size, PROGRESS_STEPS, (pos, len) -> {
                                logger.log (Level.INFO, "{0}% of {1} read...", new Object[]{Double.valueOf ((100.0 * pos) / (1.0 * len)).intValue (), postfix});
                                db.d0 (progressSQL, pos, uuid);
                            }))
                            {
                                spf.newSAXParser ().parse (pis, this);
                            }

                            if (t.getColumn ("livestatus") != null) db.d0 ("UPDATE " + t.getName () + " SET livestatus=? WHERE uuid_in_fias <> ?", 0, uuid);
                        }
                    }
                }

            }
            catch (Exception ex) {
                Logger.getLogger (InFiasMDB.class.getName()).log (Level.SEVERE, "Cannot import FIAS database", ex);
            }
        }
    }

    private class BuildingEstateScanner extends Scanner<VocBuildingEstate> {

        public BuildingEstateScanner (Map<String, Object> r, DB db, UUID uuid, Set<String> aoGuids) {
            super (r, db, uuid, aoGuids);
            tagName = "EstateStatus";
            postfix = "eststat";
        }
       
    }
    
    private class BuildingStructureScanner extends Scanner<VocBuildingStructure> {

        public BuildingStructureScanner (Map<String, Object> r, DB db, UUID uuid, Set<String> aoGuids) {
            super (r, db, uuid, aoGuids);
            tagName = "StructureStatus";
            postfix = "strstat";
        }
        
    }

    private class StreetScanner extends BScanner<VocStreet, InFiasVocStreet> {
        
        public StreetScanner (Map<String, Object> r, DB db, UUID uuid, Set<String> aoGuids) {
            super (r, db, uuid, aoGuids);
            tagName = "Object";
            postfix = "addrobj";
        }

        @Override
        final boolean isOff (Attributes attributes) {
            if (!"1".equals  (attributes.getValue ("LIVESTATUS"))) return true;            
            if (!"7".equals  (attributes.getValue ("AOLEVEL")))    return true;            
            if (!"77".equals (attributes.getValue ("REGIONCODE"))) return true;
            return false;
        }

        @Override
        final void extra (Attributes attributes) {
            aoGuids.add (attributes.getValue ("AOGUID"));
        }
        
    }
    
    private class BuildingScanner extends BScanner<VocBuilding, InFiasVocBuilding> {
        
        String now = new java.util.Date ().toInstant ().toString ().substring (0, 10);

        public BuildingScanner (Map<String, Object> r, DB db, UUID uuid, Set<String> aoGuids) {
            super (r, db, uuid, aoGuids);
            tagName = "House";
            postfix = "house";
        }

        @Override
        final boolean isOff (Attributes attributes) {

            String okato = attributes.getValue ("OKATO");
            if (okato == null || okato.length () < 2 || okato.charAt (0) != '4') return true;
            switch (okato.charAt (1)) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                    // 45, 46 OK
                case '7':
                case '8':
                case '9':
                    return true;
                default:
                    break;
            }
            
            String dtTo = attributes.getValue ("ENDDATE");
            if (dtTo != null && dtTo.compareTo (now) < 0) return true;

            if (!aoGuids.contains (attributes.getValue ("AOGUID"))) return true;

            return false;

        }

    }

}