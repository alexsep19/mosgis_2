package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocVotingForm;
import ru.eludia.products.mosgis.db.model.voc.VocVotingMeetingEligibility;

public class VotingProtocol extends EnTable {
            
    public enum c implements EnColEnum {

        UUID_ORG             (VocOrganization.class,         "Организация"),
        FIASHOUSEGUID        (VocBuilding.class,             "Дом"),
        ID_PRTCL_STATUS      (VocGisStatus.class,      VocGisStatus.DEFAULT, "Статус протокола с точки зрения mosgis"),
        ID_PRTCL_STATUS_GIS  (VocGisStatus.class,      VocGisStatus.DEFAULT, "Статус протокола с точки зрения ГИС ЖКХ"),        

        FORM_                (VocVotingForm.class,        "Форма проведения"),

        LABEL                (Type.STRING, new Virt("'№' || protocolnum || ' от ' || TO_CHAR (protocoldate, 'DD.MM.YYYY')"), "№/дата"),

        PROTOCOLNUM          (Type.STRING, 30, null,    "Номер протокола"),
        PROTOCOLDATE         (Type.DATE,                  "Дата составления протокола"),

        AVOTINGSTARTDATE     (Type.DATE, null, "Дата начала приема решений (заочное голосование опросным путем)"),
        AVOTINGDATE          (Type.DATE, null, "Дата окончания приема решений (заочное голосование опросным путем)"),
        RESOLUTIONPLACE      (Type.STRING, 3000, null, "Место принятия решений (заочное голосование опросным путем)"),

        MEETINGDATE          (Type.DATETIME, null, "Дата и время проведения собрания (очное голосование)"),
        VOTINGPLACE          (Type.STRING, 3000, null, "Место проведения собрания (очное голосование)"),

        EVOTINGDATEBEGIN     (Type.DATETIME, null, "Дата и время начала проведения голосования (заочное голосование с использованием системы)"),
        EVOTINGDATEEND       (Type.DATETIME, null, "Дата и время окончания проведения голосования (заочное голосование с использованием системы)"),
        DISCIPLINE           (Type.STRING, 2000, null, "Порядок приема оформленных в письменной форме решений собственников (заочное голосование с использованием системы)"),
        INFOREVIEW           (Type.STRING, 2000, null, "Порядок ознакомления с информацией и (или) материалами, которые будут представлены на данном собрании (заочное голосование с использованием системы)"),

        MEETING_AV_DATE      (Type.DATETIME, null, "Дата и время проведения собрания (очно-заочное голосование)"),
        MEETING_AV_PLACE     (Type.STRING, 3000, null, "Место проведения собрания (очно-заочное голосование)"),
        MEETING_AV_DATE_START(Type.DATE, null, "Дата начала приема решений (очно-заочное голосование)"),
        MEETING_AV_DATE_END  (Type.DATE, null, "Дата окончания приема решений (очно-заочное голосование)"),
        MEETING_AV_RES_PLACE (Type.STRING, 3000, null, "Место приема решения (очно-заочное голосование)"),

        EXTRAVOTING          (Type.BOOLEAN, "Внеочередное собрание"),
        ANNUALVOTING         (Type.BOOLEAN, new Virt("DECODE(\"EXTRAVOTING\",1,0,1)"), "Ежегодное собрание"),
        MEETINGELIGIBILITY   (VocVotingMeetingEligibility.class, "Правомочность собрания"),

        MODIFICATION         (Type.STRING, 2000, null, "Основание изменения (для протоколов в статусе \"Размещен\")"),

        ID_LOG               (VotingProtocolLog.class,    null,         "Последнее событие редактирования"),
        
        VOTINGPROTOCOLGUID   (Type.UUID, null,                 "UUID протокола в ГИС ЖКХ")

        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                case FIASHOUSEGUID:
                case UUID_ORG:
                    return false;
                default: 
                    return true;
            }
        }
        
        public VocVotingForm.i getVotingForm () {
            
            switch (this) {
                
                case AVOTINGSTARTDATE:
                case AVOTINGDATE:
                case RESOLUTIONPLACE:
                    return VocVotingForm.i.AVOTING;
                    
                case MEETINGDATE:
                case VOTINGPLACE:
                    return VocVotingForm.i.MEETING;
                    
                case EVOTINGDATEBEGIN:
                case EVOTINGDATEEND:
                case DISCIPLINE:
                case INFOREVIEW:
                    return VocVotingForm.i.EVOTING;
                    
                case MEETING_AV_DATE:
                case MEETING_AV_PLACE:
                case MEETING_AV_DATE_START:
                case MEETING_AV_DATE_END:
                case MEETING_AV_RES_PLACE:
                    return VocVotingForm.i.MEET_AV;
                    
                default: 
                    return null;
                    
            }
            
        }

    }

    public VotingProtocol () {
        
        super ("tb_voting_protocols", "Протоколы ОСС");
        
        cols (c.class);
        
        trigger ("BEFORE UPDATE", 
                
            "DECLARE "
            + " cnt NUMBER;"
            + " uuid_init RAW(16);"
            + "BEGIN "

                + "IF :NEW.ID_PRTCL_STATUS <> :OLD.ID_PRTCL_STATUS "
                    + " AND :OLD.ID_PRTCL_STATUS <> " + VocGisStatus.i.FAILED_PLACING.getId ()
                    + " AND :NEW.ID_PRTCL_STATUS =  " + VocGisStatus.i.PROJECT.getId ()
                + " THEN "
                    + " :NEW.ID_PRTCL_STATUS := " + VocGisStatus.i.MUTATING.getId ()
                + "; END IF; "
                        
                + " IF :NEW.ID_PRTCL_STATUS = " + VocGisStatus.i.MUTATING.getId () + " AND :NEW.MODIFICATION IS NULL THEN raise_application_error (-20000, '#modification#: Не указано основание изменения. Операция отменена.'); END IF; "

                + "IF :NEW.is_deleted=0 AND :NEW.ID_PRTCL_STATUS <> :OLD.ID_PRTCL_STATUS AND :NEW.ID_PRTCL_STATUS=" + VocGisStatus.i.PENDING_RQ_PLACING.getId () + " THEN BEGIN "

                    + "IF :NEW.FORM_=" + VocVotingForm.i.AVOTING + " AND :NEW.AVOTINGDATE > TRUNC(SYSDATE) THEN raise_application_error (-20000, 'Дата окончания приема решений не должна быть позже текущей даты.'); END IF; "
                    + "IF :NEW.FORM_=" + VocVotingForm.i.MEET_AV + " AND :NEW.MEETING_AV_DATE_END > TRUNC(SYSDATE) THEN raise_application_error (-20000, 'Дата окончания приема решений не должна быть позже текущей даты.'); END IF; "

                    + "IF :NEW.FORM_=" + VocVotingForm.i.MEETING + " AND :NEW.MEETINGDATE > TRUNC(SYSDATE) THEN raise_application_error (-20000, 'Дата проведения собрания не должна быть позже текущей даты.'); END IF; "
                    + "IF :NEW.FORM_=" + VocVotingForm.i.MEET_AV + " AND :NEW.MEETING_AV_DATE > TRUNC(SYSDATE) THEN raise_application_error (-20000, 'Дата проведения собрания не должна быть позже текущей даты.'); END IF; "

                    + " SELECT COUNT(*) INTO cnt FROM tb_voting_protocol_files WHERE id_status=1 AND UUID_PROTOCOL=:NEW.uuid; "
                    + " IF cnt=0 THEN raise_application_error (-20000, 'Файл протокола не загружен на сервер. Операция отменена.'); END IF; "
                
                    + " SELECT COUNT(*) INTO cnt FROM tb_vote_decision_lists WHERE is_deleted=0 AND PROTOCOL_UUID=:NEW.uuid; "
                    + " IF cnt=0 THEN raise_application_error (-20000, 'Не заполнена повестка собрания. Операция отменена.'); END IF; "

                    + " SELECT MIN(uuid) INTO uuid_init FROM tb_vote_initiators WHERE is_deleted = 0 AND UUID_PROTOCOL=:NEW.uuid AND UUID_ORG=:NEW.UUID_ORG; "
                    + " IF uuid_init IS NULL THEN raise_application_error (-20000, 'Вашей организации нет в списке инициаторов. Операция отменена.'); END IF; "

                    + " FOR i IN ("
                        + "SELECT "
                        + " o.uuid "
                        + "FROM "
                        + " tb_vote_decision_lists o "
                        + "WHERE o.is_deleted = 0"
                        + " AND o.PROTOCOL_UUID = :NEW.uuid "
                        + " AND o.DECISIONTYPE_VC_NSI_63 = '2.1' "
                        + " AND o.FORMINGFUND_VC_NSI_241 = '1' "
                        + ") LOOP"
                    + " raise_application_error (-20000, 'Для выбора способа формирования фонда капитального ремонта запрещено передавать «Значение не выбрано». Операция отменена.'); "
                    + " END LOOP; "

                + "END; END IF; "
            + "END;"
                
        );
        
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
    
}