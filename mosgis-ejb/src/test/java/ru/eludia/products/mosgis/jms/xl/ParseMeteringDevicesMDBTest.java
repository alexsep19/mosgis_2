package ru.eludia.products.mosgis.jms.xl;

import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;

public class ParseMeteringDevicesMDBTest extends BaseTest {
    
//    private static final UUID uuid = UUID.fromString ("86E3DD20-537E-5660-E050-007F010143A5");
    private static final UUID uuid = UUID.fromString ("86E3DD20-537E-5660-E050-007F010143A5");

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
