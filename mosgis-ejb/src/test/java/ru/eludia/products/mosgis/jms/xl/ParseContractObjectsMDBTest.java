package ru.eludia.products.mosgis.jms.xl;

import java.io.FileInputStream;
import java.io.IOException;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Ignore;
import org.junit.Test.None;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlContractObject;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;

public class ParseContractObjectsMDBTest extends BaseTest {
    
    public ParseContractObjectsMDBTest () throws Exception {
    }

    @Ignore
    @Test (expected = None.class)
    public void test () throws Exception {

//        FileInputStream fis = new FileInputStream ("c:\\projects\\mosgis\\incoming\\pauline\\ctr_obj_0.xlsx");
        
        try (DB db = model.getDb ()) {
            
            db.forFirst (db.getModel ().get (InXlFile.class, "F48DCCA62DA842689A4DCD5D73095268", "body"), (rs) -> {

                try {
                    
                    XSSFWorkbook wb = new XSSFWorkbook (rs.getBlob (1).getBinaryStream ());
                    
                    XSSFSheet s0 = wb.getSheetAt (0);

                    System.out.println ("s0.getFirstRowNum () = " + s0.getFirstRowNum ());
                    System.out.println ("s0.getLastRowNum () = " + s0.getLastRowNum ());
                    
                }
                catch (Exception ex) {
                    throw new IllegalStateException (ex);
                }

            });
            
        }                        
        
    }

    @Test (expected = None.class)
    public void test1 () throws Exception {
        
        XSSFWorkbook wb = new XSSFWorkbook (new FileInputStream ("c:\\projects\\mosgis\\incoming\\pauline\\ctr_obj_0.xlsx"));

        XSSFSheet s0 = wb.getSheetAt (0);

        for (int i = 2; i <= s0.getLastRowNum (); i ++) {

            XSSFRow row = s0.getRow (i);
            
            System.out.println (InXlContractObject.toHash ("0000", i, row));

        }                        
        
    }
    
}
