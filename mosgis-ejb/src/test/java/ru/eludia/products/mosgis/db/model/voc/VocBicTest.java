package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.products.mosgis.db.model.voc.VocBic;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.Test;

public class VocBicTest {
        
    public VocBicTest () {
    }
/*
    @Test (expected = Test.None.class)
    public void testRaw () throws Exception {        
        File file = new java.io.File ("c:/buf/___/2.xml");        
        try (FileInputStream fis = new FileInputStream (file)) {                                    
            for (Map<String, Object> i: VocBic.parseRawInputStream (fis)) System.out.println (i.toString ());            
        }        
    }
*/    
/*    
    @Test (expected = Test.None.class)
    public void testZip () throws Exception {        
        File file = new java.io.File ("c:/buf/___/20190304ED01OSBR.zip");        
        try (FileInputStream fis = new FileInputStream (file)) {                                    
            for (Map<String, Object> i: VocBic.parseZipInputStream (fis)) System.out.println (i.toString ());
        }
    }
*/    
/*    
    @Test (expected = Test.None.class)
    public void testZip () throws Exception {  
        
//        URL url = new URL ("http://cbr.ru/s/newbik");
        URL url = new URL ("file:///C:/buf/___/20190304ED01OSBR.zip");

        try (InputStream is = url.openStream ()) {
            for (Map<String, Object> i: VocBic.parseZipInputStream (is)) System.out.println (i.toString ());
        }
        
    }    
*/    
}