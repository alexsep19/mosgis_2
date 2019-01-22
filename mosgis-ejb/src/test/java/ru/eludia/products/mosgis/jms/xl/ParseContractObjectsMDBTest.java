package ru.eludia.products.mosgis.jms.xl;

import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.Test.None;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;

public class ParseContractObjectsMDBTest extends BaseTest {
    
    public ParseContractObjectsMDBTest () throws Exception {
    }

//    @Ignore
    @Test (expected = None.class)
    public void test () throws Exception {

        ParseContractObjectsMDB mdb = new ParseContractObjectsMDB ();
        
        try (DB db = model.getDb ()) {
            
            Map<String, Object> r = db.getMap (InXlFile.class, "F48DCCA62DA842689A4DCD5D73095268");
            
            mdb.handleRecord (db, (UUID) r.get ("uuid"), r);
            
        }                        
        
    }
/*
    @Test (expected = None.class)
    public void test1 () throws Exception {
        
        XSSFWorkbook wb = new XSSFWorkbook (new FileInputStream ("c:\\projects\\mosgis\\incoming\\pauline\\ctr_obj_0.xlsx"));

        XSSFSheet s0 = wb.getSheetAt (0);

        for (int i = 2; i <= s0.getLastRowNum (); i ++) {

            XSSFRow row = s0.getRow (i);
            
            System.out.println (InXlContractObject.toHash ("0000", i, row));

        }                        
        
    }
*/    
}
