package ru.eludia.products.mosgis.jmx;

import java.io.File;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;

public class UnomTest  extends BaseTest {
    
    public UnomTest () throws Exception {
    }

    @Test (expected = Test.None.class)
    public void testImport () throws Exception {
                
        try (DB db = model.getDb ()) {            
            Unom.importFile (db, new File ("c:\\projects\\mosgis\\incoming\\unom\\UKH_20181128.csv"));
        }
        
    }
    
}
