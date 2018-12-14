package ru.eludia.products.mosgis.db.model.tables;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import org.junit.Test.None;
import ru.eludia.base.DB;
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
        testType (VocVotingForm.i.AVOTING);
    }
    
    @Test (expected = None.class)
    public void testEVoting () {
        testType (VocVotingForm.i.EVOTING);
    }
    
    @Test (expected = None.class)
    public void testMeeting () {
        testType (VocVotingForm.i.MEETING);
    }

    @Test (expected = None.class)
    public void testMeetAV () {
        testType (VocVotingForm.i.MEET_AV);
    }

    private void testType (VocVotingForm.i form) {
        
        Map<String, Object> specificPart = DB.HASH (VotingProtocol.c.FORM_, form.getName ());
        
        for (VotingProtocol.c i: VotingProtocol.c.values ()) {
            VocVotingForm.i votingForm = i.getVotingForm ();
            if (votingForm == null) continue;
            specificPart.put (i.lc (), votingForm == form ? Def.NOW : null);
        }
        
        Table.Sampler sampler = table.new Sampler (commonPart, specificPart);        
        
        Map<String, Object> sample = sampler.nextHASH ();        
        
        for (int k = 0; k < sampler.getCount (); k ++) check (sampler.cutOut (sample, k));
        
    }

    private void check (final Map<String, Object> r) throws IllegalStateException {
        dump (r);
        validate (VotingProtocol.toImportVotingProtocolPlacingRequest (r));
    }
            
}
