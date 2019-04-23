package ru.eludia.products.mosgis.jms.xl;

import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;

public class ParseMeteringDevicesMDBTest extends BaseTest {
    
//    private static final UUID uuid = UUID.fromString ("871A9FEC-5607-C122-E050-007F01012B24");
    private static final UUID uuid = UUID.fromString ("872E9B86-6179-0713-E050-007F0101461F");

    ParseMeteringDevicesMDB mdb = new ParseMeteringDevicesMDB ();
    
    public ParseMeteringDevicesMDBTest () throws Exception {
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        
        try (DB db = model.getDb ()) {            
	    mdb.setStatus (db, uuid, VocFileStatus.i.PROCESSING);
            Map<String, Object> r = db.getMap (InXlFile.class, uuid);
            mdb.handleRecord (db, uuid, r);                        
        }
        
    }
    
}
