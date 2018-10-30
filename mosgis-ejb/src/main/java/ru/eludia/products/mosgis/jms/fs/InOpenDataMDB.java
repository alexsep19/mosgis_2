package ru.eludia.products.mosgis.jms.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.model.Table;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.util.ProgressInputStream;
import ru.eludia.products.mosgis.db.model.incoming.InOpenData;
import ru.eludia.products.mosgis.db.model.incoming.InOpenDataLine;
import ru.eludia.products.mosgis.db.model.incoming.InOpenDataLinesBuf;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jmx.Conf;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inOpenDataQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class InOpenDataMDB extends UUIDMDB<InOpenData> {
    
    SAXParserFactory spf = SAXParserFactory.newInstance ();
    private static final int PROGRESS_STEPS = 100;
    private static final int PACK_SIZE = 100;
    private static final int BUFF_SIZE = 1000;
    private static final FileSystem fs = FileSystems.getDefault ();

    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "*");        
    }    

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
                        
        Set<UUID> ids = new HashSet<> ();
        
        List<Scanner> scanners = new ArrayList <> ();
        
        try {
            
//            db.truncate (InOpenDataLine.class);
            
            Files.list (fs.getPath (Conf.get (VocSetting.i.PATH_OPENDATA))).forEach (path -> {
                File file = path.toFile ();
                if (file.isDirectory ()) return;
                String name = file.getName ();
                if (!name.endsWith (".xml")) return;
                if (!name.startsWith ("data-")) return;
                scanners.add (new Scanner (db, uuid, file, ids));
            });
            
        }
        catch (IOException ex) {
            throw new SQLException (ex);
        }
        
        long sum = 0;
        
        for (Scanner i: scanners) {
            i.setStart (sum);
            sum += i.getSize ();
        }

        for (Scanner i: scanners) {
            i.setTotalSize (sum);
        }
        
        db.update (InOpenData.class, HASH ("uuid", uuid, "dt_from", NOW, "sz", sum));
        
        scanners.forEach (s -> s.run ());
        
        db.update (InOpenData.class, HASH ("uuid", uuid, "dt_to_fact", NOW));
        
        db.d0 ("UPDATE " + ModelHolder.getModel ().getName (InOpenDataLine.class) + " SET is_actual=? WHERE uuid_in_open_data<>?", 0, uuid);

    }

    private class Scanner extends DefaultHandler {
        
        DB db; 
        UUID uuid;
        File file;
        Table housesTable;
        long start;
        long totalSize;
        int line = 0;
        int fn;
        
        DB.RecordBuffer b;
        Map <String, Object> record;
        int depth;
        String k;
        StringBuilder v;
        
        void init () {
            record = HASH ();
            depth = 0;
            k = null;
            v = null;
        }

        public void setStart (long start) {
            this.start = start;
        }
        
        long getSize () {
            return file.length ();
        }

        public void setTotalSize (long totalSize) {
            this.totalSize = totalSize;
        }
        
        Scanner (DB db, UUID uuid, File file, Set<UUID> ids) {
            
            this.db = db;
            this.uuid = uuid;
            this.file = file;
            String[] parts = file.getName ().replace (".xml", "").split ("-");
            fn = Integer.valueOf (parts [5]);
            
            if (fn == 1) {
                
                try {
                    
                    db.update (InOpenData.class, HASH (
                        "no",   Integer.valueOf (parts [1]),
                        "dt",   new java.sql.Timestamp (new java.util.Date (
                                    Integer.valueOf (parts [2]) - 1900, 
                                    Integer.valueOf (parts [3]) - 1, 
                                    Integer.valueOf (parts [4])
                                ).getTime ()),
                        "uuid", uuid
                    ));
                    
                }
                catch (SQLException ex) {
                    logger.log (Level.SEVERE, "", ex);
                }
                
            }
            
            init ();
        }
                
        String getFieldName (String tag) {
            switch (tag) {
                case "UNOM":    return "unom";
                case "N_FIAS":  return "fiashouseguid";
                case "ADDRESS": return "address";
                case "KAD_N":   return "kad_n";
                default:        return null;
            }
        }
                        
        @Override
        public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if ("array".equals (qName)) {
                depth ++;
                return;
            }
            else {
                if (depth != 1) return;
            }
            
            k = getFieldName (qName);
                        
            if (k != null) v = new StringBuilder ();

        }

        @Override
        public void characters (char[] ch, int start, int length) throws SAXException {            
            if (k == null) return;            
            v.append (ch, start, length);            
        }

        private void processRecord () throws SAXException {

            try {                
                record.put ("fn", fn);
                record.put ("line", ++ line);
                record.put ("uuid_in_open_data", uuid);
                b.add (record);
            }
            catch (SQLException ex) {
                throw new SAXException (ex);
            }
            
        }

        @Override
        public void endElement (String uri, String localName, String qName) throws SAXException {
            
            if ("array".equals (qName)) {
                depth --;
                if (depth > 0) return;
                processRecord ();
                init ();
            }
            else {
                if (k == null) return;
                record.put (k, v.toString ());
                k = null;
                v = null;
            }
            
        }

        public void run () {

            logger.log (Level.INFO, "" + file); 

            try (DB.RecordBuffer bb = db.new TableUpsertBuffer (InOpenDataLine.class, PACK_SIZE, InOpenDataLinesBuf.class, BUFF_SIZE)) {
                
                b = bb;
                
                final String progressSQL = "UPDATE " + getTable ().getName () + " SET rd=? WHERE uuid=?";

                try (InputStream is = new FileInputStream (file)) {
                    
                    try (ProgressInputStream pis = new ProgressInputStream (is, file.length (), PROGRESS_STEPS, (pos, len) -> {
                        
                        long p = start + pos;

                        logger.log (Level.INFO, Double.valueOf ((100.0 * p) / totalSize).intValue () + "% read.");

                        db.d0 (progressSQL, p, uuid);

                    })) {
                        
                        try (InputStreamReader isr = new InputStreamReader (pis, "windows-1251")) {
                            
                            spf.newSAXParser ().parse (new InputSource (isr), this);
                            
                        }
    
                    }

                }               
                                
            }
            catch (Exception ex) {
                Logger.getLogger (InOpenDataMDB.class.getName()).log (Level.SEVERE, "Cannot import open data", ex);
            }
            
        }

    }

}