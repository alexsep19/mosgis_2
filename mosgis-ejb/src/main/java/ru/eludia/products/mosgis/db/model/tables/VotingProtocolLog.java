package ru.eludia.products.mosgis.db.model.tables;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocVotingForm;
import ru.gosuslugi.dom.schema.integration.house_management.ImportVotingProtocolRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ProtocolType;

public class VotingProtocolLog extends GisWsLogTable {
    
    public VotingProtocolLog () {
        super ("tb_voting_protocols__log", "История изменения протоколов ОСС", VotingProtocol.class
            , EnTable.c.class
            , VotingProtocol.c.class
        );
    }
    
    public static final ImportVotingProtocolRequest toImportVotingProtocolPlacingRequest (Map<String, Object> r) {
        
        final ImportVotingProtocolRequest.Protocol protocol = VotingProtocolLog.toImportVotingProtocolRequestProtocol (r);

        final ImportVotingProtocolRequest createImportVotingProtocolRequest = new ImportVotingProtocolRequest ();
//        protocol.setPlacing (true);
        createImportVotingProtocolRequest.setProtocol (protocol);
        createImportVotingProtocolRequest.setPlacing (true);
        createImportVotingProtocolRequest.setTransportGUID (UUID.randomUUID ().toString ());
        
        return createImportVotingProtocolRequest;
        
    }

    private static final ImportVotingProtocolRequest.Protocol toImportVotingProtocolRequestProtocol (Map<String, Object> r) {

        final ImportVotingProtocolRequest.Protocol p = (ImportVotingProtocolRequest.Protocol) DB.to.javaBean (ImportVotingProtocolRequest.Protocol.class, r);
        
        if (DB.ok (r.get ("extravoting"))) {
            p.setExtraVoting (true);
            p.setAnnualVoting (null);
        }
        else {
            p.setAnnualVoting (true);
            p.setExtraVoting (null);
        }
                
        switch (VocVotingForm.i.forName (r.get ("form_").toString ())) {
            case AVOTING:
                p.setAVoting (toAvoting (r));
                break;
            case EVOTING:
                p.setEVoting (toEvoting (r));
                break;
            case MEETING:
                p.setMeeting (toMeeting (r));
                break;
            case MEET_AV:
                p.setMeetingAVoting (toMeetingAVoting (r));
                break;
                
        }
        
        for (Map<String, Object> initiator: (Collection<Map<String, Object>>) r.get ("initiators")) p.getVoteInitiators ().add (VoteInitiator.toDom (initiator));
        
        for (Map<String, Object> decision: (Collection<Map<String, Object>>) r.get ("decisions")) p.getDecisionList ().add (VoteDecisionList.toDom (decision));

        return p;

    }

    private static ProtocolType.AVoting toAvoting (Map<String, Object> r) {
        final ProtocolType.AVoting result = (ProtocolType.AVoting) DB.to.javaBean (ProtocolType.AVoting.class, r);
        for (Map<String, Object> file: (Collection<Map<String, Object>>) r.get ("files")) result.getAttachments ().add ( VotingProtocolFile.toAttachments (file));
        return result;
    }
    
    private static ProtocolType.EVoting toEvoting (Map<String, Object> r) {
        final ProtocolType.EVoting result = (ProtocolType.EVoting) DB.to.javaBean (ProtocolType.EVoting.class, r);
        for (Map<String, Object> file: (Collection<Map<String, Object>>) r.get ("files")) result.getAttachments ().add ( VotingProtocolFile.toAttachments (file));
        return result;
    }
    
    private static ProtocolType.Meeting toMeeting (Map<String, Object> r) {
        final ProtocolType.Meeting result = (ProtocolType.Meeting) DB.to.javaBean (ProtocolType.Meeting.class, r);
        for (Map<String, Object> file: (Collection<Map<String, Object>>) r.get ("files")) result.getAttachments ().add ( VotingProtocolFile.toAttachments (file));
        return result;
    }
    
    private static ProtocolType.MeetingAVoting toMeetingAVoting (Map<String, Object> r) {
        r.put ("meetingdate", r.get ("meeting_av_date"));
        r.put ("avotingdate", r.get ("meeting_av_date_end"));
        r.put ("avotingstartdate", r.get ("meeting_av_date_start"));
        r.put ("votingplace", r.get ("meeting_av_place"));
        r.put ("resolutionplace", r.get ("meeting_av_res_place"));
        final ProtocolType.MeetingAVoting result = (ProtocolType.MeetingAVoting) DB.to.javaBean (ProtocolType.MeetingAVoting.class, r);
        for (Map<String, Object> file: (Collection<Map<String, Object>>) r.get ("files")) result.getAttachments ().add ( VotingProtocolFile.toAttachments (file));
        return result;
    }    
    
}

