package ru.eludia.products.mosgis.db.model.tables;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocVotingForm;
import ru.gosuslugi.dom.schema.integration.house_management.ImportVotingProtocolRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ProtocolType;

public class VotingProtocol extends Table {
    
    public VotingProtocol () {
        
        super ("tb_voting_protocols", "Протоколы ОСС");
        
        pk  ("uuid", Type.UUID, NEW_UUID, "Ключ");
        
        fk  ("uuid_org",                  VocOrganization.class,                      "Организация");
        
        fk  ("fiashouseguid",           VocBuilding.class,                   "Глобальный уникальный идентификатор дома по ФИАС");
        col ("is_deleted",              Type.BOOLEAN, Bool.FALSE,            "1, если запись удалена; иначе 0");
        
        fk  ("id_prtcl_status",           VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (),   "Статус протокола с точки зрения mosgis");
        ref ("id_prtcl_status_gis",       VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (),   "Статус протокола с точки зрения ГИС ЖКХ");
        
        fk  ("form_", VocVotingForm.class, null, "Форма проведения");
        //col ("label_form", Type.STRING, new Virt("DECODE(\"FORM_\", 0, 'Заочное голосование (опросным путем)', 1, 'Очное голосование', 2, 'Заочное голосование с использованием системы', 3, 'Очно-заочное голосование', 'Неизвестная форма проведения')"), "Форма проведения");
        //col ("label_form_uc", Type.STRING, new Virt("UPPER(\"LABEL_FORM\")"), "ФОРМА ПРОВЕДЕНИЯ");
        
        col ("protocolnum", Type.STRING, 30, null, "Номер протокола");
        col ("protocoldate", Type.DATE, "Дата составления протокола");
        
        col ("label", Type.STRING, new Virt("'№' || protocolnum || ' от ' || TO_CHAR (protocoldate, 'DD.MM.YYYY')"), "№/датаы");
        
        col ("avotingdate", Type.DATE, null, "Дата окончания приема решений (заочное голосование опросным путем)");
        col ("resolutionplace", Type.STRING, 3000, null, "Место принятия решений (заочное голосование опросным путем)");
        
        col ("meetingdate", Type.DATETIME, null, "Дата и время проведения собрания (очное голосование)");
        col ("votingplace", Type.STRING, 3000, null, "Место проведения собрания (очное голосование)");
        
        col ("evotingdatebegin", Type.DATETIME, null, "Дата и время начала проведения голосования (заочное голосование с использованием системы)");
        col ("evotingdateend", Type.DATETIME, null, "Дата и время окончания проведения голосования (заочное голосование с использованием системы)");
        col ("discipline", Type.STRING, 2000, null, "Порядок приема оформленных в письменной форме решений собственников (заочное голосование с использованием системы)");
        col ("inforeview", Type.STRING, 2000, null, "Порядок ознакомления с информацией и (или) материалами, которые будут представлены на данном собрании (заочное голосование с использованием системы)");
        
        col ("meeting_av_date", Type.DATETIME, null, "Дата и время проведения собрания (очно-заочное голосование)");
        col ("meeting_av_place", Type.STRING, 3000, null, "Место проведения собрания (очно-заочное голосование)");
        col ("meeting_av_date_end", Type.DATE, null, "Дата окончания приема решений (очно-заочное голосование)");
        col ("meeting_av_res_place", Type.STRING, 3000, null, "Место приема решения (очно-заочное голосование)");

        col ("extravoting", Type.BOOLEAN, "Внеочередное собрание");
        col ("annualvoting", Type.BOOLEAN, new Virt("DECODE(\"EXTRAVOTING\",1,0,1)"), "Ежегодное собрание");
        col ("meetingeligibility", Type.STRING, 1, "Правомочность собрания. (C)OMPETENT - правомочно, (N)OT_COMPETENT - не правомочно");
        
        col ("modification", Type.STRING, 2000, null, "Основание изменения (для протоколов в статусе \"Размещен\")");
        
        fk  ("id_log",                VotingProtocolLog.class,    null,         "Последнее событие редактирования");
    }
    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
        ANNULMENT   (VocGisStatus.i.PENDING_RP_ANNULMENT, VocGisStatus.i.ANNUL,    VocGisStatus.i.FAILED_ANNULMENT)
        ;
        
        VocGisStatus.i nextStatus;
        VocGisStatus.i okStatus;
        VocGisStatus.i failStatus;

        private Action (VocGisStatus.i nextStatus, VocGisStatus.i okStatus, VocGisStatus.i failStatus) {
            this.nextStatus = nextStatus;
            this.okStatus = okStatus;
            this.failStatus = failStatus;
        }

        public VocGisStatus.i getNextStatus () {
            return nextStatus;
        }

        public VocGisStatus.i getFailStatus () {
            return failStatus;
        }

        public VocGisStatus.i getOkStatus () {
            return okStatus;
        }
        
        public static Action forStatus (VocGisStatus.i status) {
            switch (status) {
                case PENDING_RQ_PLACING:   return PLACING;
                case PENDING_RQ_ANNULMENT: return ANNULMENT;
                default: return null;
            }            
        }
        
        public static Action forLogAction (VocAction.i a) {
            switch (a) {
                case APPROVE: return PLACING;
                case ANNUL:   return ANNULMENT;
                default: return null;
            }            
        }
                        
    };

    private static final Logger logger = Logger.getLogger (VotingProtocol.class.getName ());    

    public static final ImportVotingProtocolRequest.Protocol toDom (Map<String, Object> r) {

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

}