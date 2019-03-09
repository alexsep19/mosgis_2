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
import ru.eludia.products.mosgis.db.model.DataSourceImpl;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.TestRuleImpl;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.ModelHolder;

public class BaseTest {
    
    protected static JsonWriterFactory jwf;
    protected JAXBContext jc;
    protected Schema schema;
    protected MosGisModel model;
    protected ModelHolder modelHolder;   
    
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
        modelHolder = new ModelHolder (new DataSourceImpl (getCn ()));
        model = modelHolder.getModel ();
        try (DB db = model.getDb ()) {
            db.adjustModel ();
        }
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
    
    protected String getSomeUuid (Class c) throws SQLException {
        try (DB db = model.getDb ()) {
            return db.getString (model.select (c, "uuid").where ("is_deleted", 0));
        }
    }
    
    protected String getSomeFiasHouseGuid () throws SQLException {
        try (DB db = model.getDb ()) {
            return db.getString (model.select (VocBuilding.class, "houseguid"));
        }
    }
    
    protected String getOrgUuid () throws SQLException {
        return getSomeUuid (VocOrganization.class);
    }
    
    protected String getPersonUuid () throws SQLException {
        try (DB db = model.getDb ()) {
            return db.getString (model.select (VocPerson.class, "uuid").where ("is_deleted", 0).and ("code_vc_nsi_95 IS NOT NULL"));
        }
    }
    
}
