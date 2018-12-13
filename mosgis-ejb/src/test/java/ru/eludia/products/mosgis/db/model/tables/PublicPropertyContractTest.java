package ru.eludia.products.mosgis.db.model.tables;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.DataSourceImpl;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.TestRuleImpl;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.house_management.ImportPublicPropertyContractRequest;

public class PublicPropertyContractTest {
    
    private static MosGisModel m;
    private static JAXBContext jc;
    private static Schema schema;
    private static Table table;
    private static Map<String, Object> commonPart;
    private static JsonWriterFactory jwf;
    
    @ClassRule
    public static TestRule classRule = new TestRuleImpl ();
    private static Connection getCn () {
        return ((TestRuleImpl) classRule).getCn ();
    }
    
    @BeforeClass
    public static void setUpClass () throws Exception {
        
        m = new MosGisModel (new DataSourceImpl (getCn ()));
        jc = JAXBContext.newInstance (ImportPublicPropertyContractRequest.class);
        table = m.get (PublicPropertyContract.class);
        
        Map<String, Boolean> config = new HashMap<> ();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        jwf = Json.createWriterFactory (config);
        schema = AbstactServiceAsync.loadSchema ("house-management/hcs-house-management-types.xsd");
        
        commonPart = HASH (
            "id_log", null
        );
        
    }
    
    @AfterClass
    public static void tearDownClass () throws SQLException {
        if (getCn () != null) getCn ().close ();
    }
    
    public PublicPropertyContractTest () {
    }

    @Test
    public void test () {
        
        Table.Sampler sampler = table.new Sampler (commonPart);
        
        Map<String, Object> sample = sampler.nextHASH ();
        
        for (int k = 0; k < sampler.getCount (); k ++) {
            
            final Map<String, Object> r = sampler.cutOut (sample, k);

            try (JsonWriter jw = jwf.createWriter (System.out)) {
                jw.writeObject ((JsonObject) DB.to.json (r));
            }            
                        
            validate (PublicPropertyContract.toImportPublicPropertyContractRequest (r));
            
        }
        
    }
    
    private void validate (final Object rq) throws IllegalStateException {
                
        StringWriter sw = new StringWriter ();
        
        try {
            
            final Marshaller mar = jc.createMarshaller ();

            mar.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            mar.marshal (rq, sw);

            System.out.println (sw);

            mar.setSchema (schema);
            mar.marshal (rq, sw);
            
        }
        catch (Exception ex) {
            throw new IllegalStateException (ex);
        }
        
    }
    
}
