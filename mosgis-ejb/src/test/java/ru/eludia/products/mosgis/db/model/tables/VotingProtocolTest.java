package ru.eludia.products.mosgis.db.model.tables;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.ClassRule;
import org.junit.Assert;
import org.junit.rules.TestRule;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.DataSourceImpl;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.TestRuleImpl;
import ru.eludia.products.mosgis.db.model.voc.VocVotingForm;
import ru.gosuslugi.dom.schema.integration.house_management.ImportVotingProtocolRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ProtocolType;

public class VotingProtocolTest {
    
    private static final Logger logger = Logger.getLogger (VotingProtocolTest.class.getName ());
    
    private static MosGisModel m;
    private static JAXBContext jc;
    private static SchemaFactory schemaFactory;
    private static Schema schema;
        
    @ClassRule
    public static TestRule classRule = new TestRuleImpl ();
    private static Connection getCn () {
        return ((TestRuleImpl) classRule).getCn ();
    }
    
    @BeforeClass
    public static void setUpClass () throws Exception {
        System.setProperty ("jdk.xml.maxOccurLimit", "100000");
        schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        m = new MosGisModel (new DataSourceImpl (getCn ()));
        jc = JAXBContext.newInstance (ImportVotingProtocolRequest.class);
        URL resource = m.getClass ().getClassLoader ().getResource ("META-INF/wsdl/house-management/hcs-house-management-types.xsd");
        File file = new File (resource.toURI ());
        schema = schemaFactory.newSchema (file);
    }
    
    @AfterClass
    public static void tearDownClass () throws SQLException {
        if (getCn () != null) getCn ().close ();
    }
        
    @Test
    public void testMethod () throws Exception {
        
        Table t = m.get (VotingProtocol.class);
        
        for (int i = 0; i < 10; i ++) {
            Map<String, Object> r = createAVoting (t);
            process (r);            
        }

    }

    private Map<String, Object> createAVoting (Table t) {
        Map<String, Object> r = t.randomHASH (DB.HASH (
            "form_", VocVotingForm.i.AVOTING.getName ()
        ));
        return r;
    }

    private void process (Map<String, Object> r) throws JAXBException {
        fix (r);
        dump (r);
        checkRecord (r);
    }

    private void fix (Map<String, Object> r) {
        
        r.put ("extravoting", !DB.ok (r.get ("annualvoting")));
        
        Table ft = m.get (VotingProtocolFile.class);       
        r.put ("files", Collections.singletonList (ft.randomHASH (DB.HASH ())));
        
        Table it = m.get (VoteInitiator.class);       
        r.put ("initiators", Collections.singletonList (it.randomHASH (DB.HASH ())));
        
        Table dt = m.get (VoteDecisionList.class);       
        r.put ("decisions", Collections.singletonList (dt.randomHASH (DB.HASH (
                
            VoteDecisionList.c.VOTINGRESUME, "M",
                
            "vc_nsi_25.code", "00025",
            "vc_nsi_25.guid", UUID.randomUUID (),

            "vc_nsi_241.code", "00241",
            "vc_nsi_241.guid", UUID.randomUUID (),

            "vc_nsi_63.code", "00063",
            "vc_nsi_63.guid", UUID.randomUUID ()
                
        ))));
                
    }

    private void dump (Map<String, Object> r) {
        System.out.println ('{');
        r.keySet ().stream ().sorted ().forEach ((k) -> {
            System.out.println (" " + k + " = " + r.get (k));
        });
        System.out.println ('}');
    }
    
    private void fix (ProtocolType.DecisionList t) {
        if (t.getQuestionNumber () < 0) t.setQuestionNumber (-t.getQuestionNumber ());
    }

    private void fix (ImportVotingProtocolRequest.Protocol p) {
        p.getDecisionList ().forEach ((t) -> {fix (t);});
        p.setMeetingEligibility ("C");
    }    

    private void checkRecord (Map<String, Object> r) throws JAXBException, PropertyException {
        
        final ImportVotingProtocolRequest.Protocol protocol = VotingProtocol.toDom (r);
        
        fix (protocol);
        
        final ImportVotingProtocolRequest rq = new ImportVotingProtocolRequest ();
        rq.setProtocol (protocol);
        rq.setTransportGUID (UUID.randomUUID ().toString ());
        StringWriter sw = new StringWriter ();
        final Marshaller mar = jc.createMarshaller ();
        mar.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        mar.marshal (rq, sw);
        System.out.println (sw);
        
        mar.setSchema (schema);
        mar.marshal (rq, sw);
    }
    
}
