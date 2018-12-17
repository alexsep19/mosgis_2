package ru.eludia.products.mosgis.db.model.tables.base;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.DataSourceImpl;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.TestRuleImpl;

public class BaseTest {
    
    private static JsonWriterFactory jwf;
    protected JAXBContext jc;
    protected Schema schema;
    protected MosGisModel model;
    
    static {
        Map<String, Boolean> config = new HashMap<> ();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        jwf = Json.createWriterFactory (config);
    }

    @ClassRule
    public static TestRule classRule = new TestRuleImpl ();
    private Connection getCn () {
        return ((TestRuleImpl) classRule).getCn ();
    }
    
    @AfterClass
    public static void tearDownClass () throws SQLException {
        ((TestRuleImpl) classRule).closeConnection ();
    }    
    
    public BaseTest () throws SQLException, IOException {
        model = new MosGisModel (new DataSourceImpl (getCn ()));
    }
    
    protected void dump (final Map<String, Object> r) {
        
        try (JsonWriter jw = jwf.createWriter (System.out)) {
            jw.writeObject ((JsonObject) DB.to.json (r));
        }
        
    }
    
    protected void validate (final Object rq) throws IllegalStateException {
                
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
