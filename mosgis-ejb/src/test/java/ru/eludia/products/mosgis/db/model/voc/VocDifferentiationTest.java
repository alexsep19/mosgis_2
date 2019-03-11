package ru.eludia.products.mosgis.db.model.voc;

import java.io.File;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.impl.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.tariff.ExportDifferentiationType;
//import ru.gosuslugi.dom.schema.integration.tariff.ExportDifferentiationType;
import ru.gosuslugi.dom.schema.integration.tariff.GetStateResult;

public class VocDifferentiationTest extends BaseTest {
    
    public VocDifferentiationTest () throws Exception {
        super ();
        jc = JAXBContext.newInstance (GetStateResult.class);
        schema = AbstactServiceAsync.loadSchema ("tariff/hcs-tariff-types.xsd");
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        
        Unmarshaller um = jc.createUnmarshaller ();
        um.setSchema (schema);
        Object o = um.unmarshal (new File ("c:/buf/___/tar.xml"));
        
        GetStateResult getStateResult = (GetStateResult) o;
        final List<ExportDifferentiationType> diffs = getStateResult.getExportTariffDifferentiationResult ().get (0).getDifferentiation ();
        
        System.out.println (diffs.size ());
        
        try (DB db = model.getDb ()) {            
            for (ExportDifferentiationType diff: diffs) VocDifferentiation.store (db, diff);
        }        
        
    }
    
}
