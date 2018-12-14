package ru.eludia.products.mosgis.db.model.tables;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import org.junit.Test.None;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.def.Def;
import ru.eludia.products.mosgis.db.model.voc.VocVotingForm;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.house_management.ImportVotingProtocolRequest;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;

public class VotingProtocolTest extends BaseTest {
        
    private Table table;
    private Map<String, Object> commonPart;

    public VotingProtocolTest () throws Exception {
        super ();
        jc = JAXBContext.newInstance (ImportVotingProtocolRequest.class);
        schema = AbstactServiceAsync.loadSchema ("house-management/hcs-house-management-types.xsd");
        table = model.get (VotingProtocol.class);        
        commonPart = HASH (    
                
            "id_log", null,
                
            "annualvoting", 0,
            "meetingeligibility", "C",
            "fiashouseguid", UUID.randomUUID (),
                
            "initiators", Collections.singletonList (model.get (VoteInitiator.class).new Sampler ().nextHASH ()),                
            "files", Collections.singletonList (model.get (VotingProtocolFile.class).new Sampler ().nextHASH ()),            
            "decisions", Collections.singletonList (model.get (VoteDecisionList.class).new Sampler (HASH (
                    
                VoteDecisionList.c.VOTINGRESUME, "M",

                "vc_nsi_25.code", "00025",
                "vc_nsi_25.guid", UUID.randomUUID (),

                "vc_nsi_241.code", "00241",
                "vc_nsi_241.guid", UUID.randomUUID (),

                "vc_nsi_63.code", "00063",
                "vc_nsi_63.guid", UUID.randomUUID ()
                    
            )).nextHASH ())
                
        );        
        
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
        Table.Sampler sampler = table.new Sampler (commonPart, specificPart);        
        Map<String, Object> sample = sampler.nextHASH ();        
        for (int k = 0; k < sampler.getCount (); k ++) check (sampler.cutOut (sample, k));
    }

    private void check (final Map<String, Object> r) throws IllegalStateException {
        dump (r);
        validate (VotingProtocol.toImportVotingProtocolPlacingRequest (r));
    }
            
}
