package ru.eludia.products.mosgis.jms.fs;

import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.incoming.InFias;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.jmx.Fias;

public class InFiasMDBTest extends BaseTest {
    
    InFiasMDB mdb = new InFiasMDB ();
    Fias fias = new Fias ();
    
    public InFiasMDBTest () throws Exception {
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        
        try (DB db = model.getDb ()) {
            
            Fias.Import imp = fias.new Import ("C:/projects/mosgis/incoming/fias");
            
            UUID uuid = (UUID) db.insertId (InFias.class, imp.getRecord ());
            
            Map<String, Object> r = db.getMap (model.get (InFias.class, uuid, "*"));
            
            mdb.handleRecord (db, uuid, r);
            
        }
        
    }
    
}
