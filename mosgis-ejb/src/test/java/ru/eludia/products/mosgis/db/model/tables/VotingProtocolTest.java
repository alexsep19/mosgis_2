package ru.eludia.products.mosgis.db.model.tables;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.ClassRule;
import org.junit.Test.None;
import org.junit.rules.TestRule;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.def.Def;
import ru.eludia.products.mosgis.db.model.DataSourceImpl;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.TestRuleImpl;
import ru.eludia.products.mosgis.db.model.voc.VocVotingForm;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.house_management.ImportVotingProtocolRequest;

public class VotingProtocolTest {
        
    private static MosGisModel m;
    private static JAXBContext jc;
    private static Schema schema;
    private static Table votingProtocolTable;
    private static Map<String, Object> commonPart;
        
    @ClassRule
    public static TestRule classRule = new TestRuleImpl ();
    private static Connection getCn () {
        return ((TestRuleImpl) classRule).getCn ();
    }
    
    @BeforeClass
    public static void setUpClass () throws Exception {
        
        m = new MosGisModel (new DataSourceImpl (getCn ()));
        jc = JAXBContext.newInstance (ImportVotingProtocolRequest.class);
        votingProtocolTable = m.get (VotingProtocol.class);
        
        commonPart = HASH (    
                
            "id_log", null,
                
            "annualvoting", 0,
            "meetingeligibility", "C",
                
            "initiators", Collections.singletonList (m.get (VoteInitiator.class).new Sampler ().nextHASH ()),
                
            "files", Collections.singletonList (m.get (VotingProtocolFile.class).new Sampler ().nextHASH ()),
            
            "decisions", Collections.singletonList (m.get (VoteDecisionList.class).new Sampler (HASH (
                    
                VoteDecisionList.c.VOTINGRESUME, "M",

                "vc_nsi_25.code", "00025",
                "vc_nsi_25.guid", UUID.randomUUID (),

                "vc_nsi_241.code", "00241",
                "vc_nsi_241.guid", UUID.randomUUID (),

                "vc_nsi_63.code", "00063",
                "vc_nsi_63.guid", UUID.randomUUID ()
                    
            )).nextHASH ())
                
        );

        schema = AbstactServiceAsync.loadSchema ("house-management/hcs-house-management-types.xsd");
        
    }
    
    @AfterClass
    public static void tearDownClass () throws SQLException {
        if (getCn () != null) getCn ().close ();
    }
            
    @Test (expected = None.class)
    public void testAVoting () {
                
        testType (HASH (
                
            "form_", VocVotingForm.i.AVOTING.getName (),
                
            "avotingdate", Def.NOW,
            "resolutionplace", Def.NOW,
                
            "meetingdate", null,
            "votingplace", null,

            "evotingdatebegin", null,
            "evotingdateend", null,
            "discipline", null,
            "inforeview", null,

            "meeting_av_date", null,
            "meeting_av_place", null,
            "meeting_av_date_end", null,
            "meeting_av_res_place", null
            
        ));
        
    }
    
    @Test (expected = None.class)
    public void testEVoting () {
                
        testType (HASH (
                
            "form_", VocVotingForm.i.EVOTING.getName (),
                
            "avotingdate", null,
            "resolutionplace", null,
                
            "meetingdate", null,
            "votingplace", null,

            "evotingdatebegin", Def.NOW,
            "evotingdateend", Def.NOW,
            "discipline", Def.NOW,
            "inforeview", Def.NOW,

            "meeting_av_date", null,
            "meeting_av_place", null,
            "meeting_av_date_end", null,
            "meeting_av_res_place", null
            
        ));
                
    }
    
    @Test (expected = None.class)
    public void testMeeting () {
                
        testType (HASH (
                
            "form_", VocVotingForm.i.MEETING.getName (),
                
            "avotingdate", null,
            "resolutionplace", null,
                
            "meetingdate", Def.NOW,
            "votingplace", Def.NOW,

            "evotingdatebegin", null,
            "evotingdateend", null,
            "discipline", null,
            "inforeview", null,

            "meeting_av_date", null,
            "meeting_av_place", null,
            "meeting_av_date_end", null,
            "meeting_av_res_place", null
            
        ));
                
    }

    @Test (expected = None.class)
    public void testMeetAV () {
                
        testType (HASH (
                
            "form_", VocVotingForm.i.MEET_AV.getName (),
                
            "avotingdate", null,
            "resolutionplace", null,
                
            "meetingdate", null,
            "votingplace", null,

            "evotingdatebegin", null,
            "evotingdateend", null,
            "discipline", null,
            "inforeview", null,

            "meeting_av_date", Def.NOW,
            "meeting_av_place", Def.NOW,
            "meeting_av_date_end", Def.NOW,
            "meeting_av_res_place", Def.NOW
            
        ));
                
    }
    
    
    private void testType (Map<String, Object> specificPart) {
        
        Table.Sampler sampler = votingProtocolTable.new Sampler (commonPart, specificPart);
        
        Map<String, Object> sample = sampler.nextHASH ();
        
        for (int k = 0; k < sampler.getCount (); k ++) {
            
            final Map<String, Object> r = sampler.cutOut (sample, k);
            
            dump (r);
            
            final ImportVotingProtocolRequest.Protocol p = VotingProtocol.toDom (r);

            p.getDecisionList ().forEach ((t) -> {
                if (t.getQuestionNumber () < 0) t.setQuestionNumber (-t.getQuestionNumber ());
            });

            final ImportVotingProtocolRequest rq = new ImportVotingProtocolRequest ();
            rq.setProtocol (p);
            rq.setTransportGUID (UUID.randomUUID ().toString ());

            validate (rq);
            
        }
        
    }   
        
    private void dump (Map<String, Object> r) {
        System.out.println ('{');
        r.keySet ().stream ().sorted ().forEach ((k) -> {
            System.out.println (" " + k + " = " + r.get (k));
        });
        System.out.println ('}');
    }

    private void validate (final ImportVotingProtocolRequest rq) throws IllegalStateException {
                
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
