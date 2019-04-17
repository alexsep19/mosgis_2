package ru.eludia.products.mosgis.jms.xl;

import java.util.Map;
import java.util.UUID;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;

public class ParseMeteringValuesMDBTest extends BaseTest {
    
//    private static final UUID uuid = UUID.fromString ("86A3D973-518E-C1AF-E050-007F01013F66");
    private static final UUID uuid = UUID.fromString ("86B63CBD-A7B4-125D-E050-007F01011C30");
//    private static final UUID uuid = UUID.fromString ("86A6A439-A2F9-4EE6-E050-007F01012A8B");


    ParseMeteringValuesMDB mdb = new ParseMeteringValuesMDB ();
    
    public ParseMeteringValuesMDBTest () throws Exception {
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        
        try (DB db = model.getDb ()) {            
	    mdb.setStatus (db, uuid, VocFileStatus.i.PROCESSING);
            Map<String, Object> r = db.getMap (InXlFile.class, uuid);
            mdb.handleRecord (db, uuid, r);       
            assertTrue(true);
        }catch(Exception e){
            assertTrue(false);
        }
        
    }
    
}
