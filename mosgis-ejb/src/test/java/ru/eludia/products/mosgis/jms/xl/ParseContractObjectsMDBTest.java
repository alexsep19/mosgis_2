package ru.eludia.products.mosgis.jms.xl;

import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.Test.None;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;

public class ParseContractObjectsMDBTest extends BaseTest {
    
    private static final UUID uuid = UUID.fromString ("40c88162-0401-45b2-b767-c602c17195b4");

    public ParseContractObjectsMDBTest () throws Exception {
    }

//    @Ignore
    @Test (expected = None.class)
    public void test () throws Exception {

        ParseContractObjectsMDB mdb = new ParseContractObjectsMDB ();
        
        try (DB db = model.getDb ()) {
            
            mdb.setStatus (db, uuid, VocFileStatus.i.PROCESSING);
            
            Map<String, Object> r = db.getMap (InXlFile.class, uuid);
            
            mdb.handleRecord (db, uuid, r);
                    
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
