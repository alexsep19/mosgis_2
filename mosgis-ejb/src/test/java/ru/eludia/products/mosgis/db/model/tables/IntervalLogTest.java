package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.volume_quality.ImportIntervalRequest;

public class IntervalLogTest extends BaseTest {
    
    Interval table;
    IntervalLog logTable;
    
    public IntervalLogTest () throws Exception {
        
        super ();
        jc = JAXBContext.newInstance (ImportIntervalRequest.class);
        schema = SOAPTools.loadSchema ("volume-quality/hcs-volume-quality-types.xsd");
        
        table = (Interval) model.get (Interval.class);
        logTable = (IntervalLog) model.get (IntervalLog.class);
        
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        
        Map<String, Object> r = getData ();
        
        r.put ("INTERVALGUID", null);

        dump (r);

        validate (IntervalLog.toImportIntervalRequest (r));
        
        r.put ("INTERVALGUID", UUID.randomUUID ());

        dump (r);

        validate (IntervalLog.toImportIntervalRequest (r));
        
    }

    private Map<String, Object> getData () throws SQLException {
        
        try (DB db = model.getDb ()) {
            
            final Map<String, Object> r = db.getMap (logTable.getForExport ("81c19ef4-c280-19fe-e053-0100007f83f4"));
            
            IntervalLog.addItemsForExport (db, r);
            
            return r;

        }
        
    }
    
}
