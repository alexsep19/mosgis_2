package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;

public class ParsePaymentsMDBTest extends BaseTest {
    
    private static final UUID uuid = UUID.fromString ("8651FF5B42A919B9E0530D0B000A8186");

    ParsePaymentsMDB mdb = new ParsePaymentsMDB ();
    
    public ParsePaymentsMDBTest () throws Exception {
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        
        try (DB db = model.getDb ()) {            
	    mdb.setStatus (db, uuid, VocFileStatus.i.PROCESSING);
            Map<String, Object> r = db.getMap (InXlFile.class, uuid);
            mdb.handleRecord (db, uuid, r);                        
        }
        
    }
    
    @Before
    public void clean() throws SQLException {

	try (DB db = model.getDb()) {
	    db.d0(new QP("DELETE FROM in_xl_payments WHERE uuid_xl = ?", uuid));
	}

    }
}
