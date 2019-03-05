package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.products.mosgis.db.model.voc.VocBic;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import javax.xml.parsers.SAXParserFactory;
import org.junit.Test;

public class VocBicTest {
    
    SAXParserFactory spf = SAXParserFactory.newInstance ();
    
    public VocBicTest () {
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        
        File file = new java.io.File ("c:/buf/___/2.xml");
        try (FileInputStream fis = new FileInputStream (file)) {
            
            VocBic.SAXHandler h = new VocBic.SAXHandler ();
            
            spf.newSAXParser ().parse (fis, h);
            
            for (Map<String, Object> i: h.getCorrAccounts ()) System.out.println (i.toString ());
            
        }
        
    }
    
}
