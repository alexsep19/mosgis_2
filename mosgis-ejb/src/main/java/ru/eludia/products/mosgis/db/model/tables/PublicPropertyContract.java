package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocPublicPropertyContractFileType;


public class PublicPropertyContract extends EnTable {

    public enum c implements EnColEnum {

        UUID_ORG             (VocOrganization.class,     "Организация-исполнитель"),
        FIASHOUSEGUID        (VocBuilding.class,         "Дом"),
        UUID_ORG_CUSTOMER    (VocOrganization.class,     "Организация-заказчик"),
        UUID_PERSON_CUSTOMER (VocPerson.class,           "Физлицо-заказчик"),
        ID_CTR_STATUS        (VocGisStatus.class,        VocGisStatus.i.PROJECT.asDef (),     "Статус устава с точки зрения mosgis"),
        ID_CTR_STATUS_GIS    (VocGisStatus.class,        VocGisStatus.i.PROJECT.asDef (),     "Статус устава с точки зрения ГИС ЖКХ"),
        ID_CTR_STATE_GIS     (VocGisStatus.class,        VocGisStatus.i.NOT_RUNNING.asDef (), "Состояние устава с точки зрения ГИС ЖКХ"),

        ID_LOG               (PublicPropertyContractLog.class,  "Последнее событие редактирования"),
        
        CONTRACTNUMBER       (Type.STRING, 255,    null,       "Номер договора"),
        DATE_                (Type.DATE,                       "Дата договора"),
        STARTDATE            (Type.DATE,                       "Дата начала действия договора"),
        ENDDATE              (Type.DATE,                       "Планируемая дата окончания действия договора"),
        CONTRACTOBJECT       (Type.STRING, 255,    null,       "Предмет договора"),
        COMMENTS             (Type.STRING, 255,    null,       "Комментарий"),
        PAYMENT              (Type.NUMERIC, 10, 2, null,       "Размер платы за предоставление в пользование части общего имущества собственников помещений в МКД в месяц"),
        MONEYSPENTDIRECTION  (Type.STRING, 255,    null,       "Направление расходования средств, внесенных за пользование частью общего имущества"),
        
        DDT_START            (Type.NUMERIC, 2,     null,       "Начало периода внесения платы по договору (1..31 — конкретное число; 99 — последнее число)"),
        DDT_START_NXT        (Type.BOOLEAN,        Bool.FALSE, "1, если начало периода внесения платы по договору в следующем месяце; иначе 0"),
        DDT_END              (Type.NUMERIC, 2,     null,       "Окончание периода внесения платы по договору (1..31 — конкретное число; 99 — последнее число)"),
        DDT_END_NXT          (Type.BOOLEAN,        Bool.FALSE, "1, если окончание периода внесения платы по договору в следующем месяце; иначе 0"),
        IS_OTHER             (Type.BOOLEAN,        Bool.FALSE, "1, если период внесеняи платы — \"иной\"; иначе 0"),
        OTHER                (Type.STRING,  500,   null,       "Иное (период внесения платы)"),
        
        ISGRATUITOUSBASIS    (Type.BOOLEAN,        Bool.TRUE, "1, если договор заключен на безвозмездной основе; иначе 0"),
        
        REASONOFANNULMENT    (Type.STRING,  1000,  null,       "Причина аннулирования"),
        IS_ANNULED           (Type.BOOLEAN,        new Virt ("DECODE(\"REASONOFANNULMENT\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0"),
        
        CONTRACTVERSIONGUID  (Type.UUID, null, "Идентификатор версии ДОГПОИ в ГИС ЖКХ")

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
                case UUID_ORG:
                case UUID_ORG_CUSTOMER:
                case UUID_PERSON_CUSTOMER:
                case FIASHOUSEGUID:
                    return false;
                default: 
                    return true;
            }
        }

    }

    public PublicPropertyContract () {
        
        super ("tb_pp_ctr", "Договор на пользование общим имуществом");

        cols   (c.class);
        
        key    ("uuid_org", c.UUID_ORG);
        
        trigger ("AFTER UPDATE", ""
            + "BEGIN "
            + " IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS "
                + " AND :NEW.ID_CTR_STATUS =  " + VocGisStatus.i.ANNUL.getId ()
            + " THEN "
                + " UPDATE tb_pp_ctr_ap SET ID_AP_STATUS=:NEW.ID_CTR_STATUS, ID_AP_STATUS_GIS=:NEW.ID_CTR_STATUS WHERE UUID_CTR=:NEW.UUID;"
            + " END IF; "
            + "END;"
        );
                
        trigger ("BEFORE UPDATE", 
                
            "DECLARE "
            + " cnt NUMBER;"
            + " uuid_init RAW(16);"
            + "BEGIN "

                + "IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS "
                    + " AND :OLD.ID_CTR_STATUS <> " + VocGisStatus.i.FAILED_PLACING.getId ()
                    + " AND :NEW.ID_CTR_STATUS =  " + VocGisStatus.i.PROJECT.getId ()
                + " THEN "
                    + " :NEW.ID_CTR_STATUS := " + VocGisStatus.i.MUTATING.getId ()
                + "; END IF; "
                        
                + "IF :NEW.is_deleted=0 AND :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS AND :NEW.ID_CTR_STATUS=" + VocGisStatus.i.PENDING_RQ_PLACING.getId () + " THEN BEGIN "

                    + " IF :NEW.isgratuitousbasis=1 THEN raise_application_error (-20000, 'Отправка в ГИС ЖКХ договоров безвозмездного пользования временно недоступна. Операция отменена.'); END IF; "

                    + " IF :NEW.ENDDATE<SYSDATE THEN raise_application_error (-20000, 'Для договора указанная дата окончания действия меньше текущей даты. Договоры с истекшим сроком действия не размещаются'); END IF; "

                    + " SELECT COUNT(*) INTO cnt FROM tb_houses WHERE fiashouseguid=:NEW.fiashouseguid AND gis_guid IS NOT NULL; "
                    + " IF cnt<>1 THEN raise_application_error (-20000, 'Первоначально необходимо разместить паспорт дома в ГИС ЖКХ. Операция отменена.'); END IF; "

                    + " SELECT COUNT(*) INTO cnt FROM vw_ca_ch_objects WHERE FIASHOUSEGUID=:NEW.FIASHOUSEGUID; "
                    + " IF cnt=0 THEN FOR i IN (SELECT label FROM vc_buildings WHERE houseguid=:NEW.FIASHOUSEGUID) LOOP "
                        + "raise_application_error (-20000, 'Для адреса дома '||i.label||' не указан действующий договор управления/устав'); "
                    + "END LOOP; END IF; "                        

                    + " SELECT COUNT(*) INTO cnt FROM tb_pp_ctr_files WHERE id_type=" + VocPublicPropertyContractFileType.i.CONTRACT + " AND id_status=1 AND UUID_CTR=:NEW.uuid; "
                    + " IF cnt=0 THEN raise_application_error (-20000, 'Файл договора не загружен на сервер. Операция отменена.'); END IF; "

                    + " cnt:=0; "
                    + " FOR i IN ("
                        + "SELECT "
                        + " vp.votingprotocolguid guid "
                        + " , vp.PROTOCOLNUM no "
                        + " , TO_CHAR (vp.PROTOCOLDATE, 'YYYY-MM-DD') dt "
                        + "FROM "
                        + " tb_pp_ctr_vp o "
                        + " INNER JOIN tb_voting_protocols vp ON o.UUID_VP=vp.UUID "
                        + "WHERE o.is_deleted = 0 "
                        + " AND vp.is_deleted = 0 "
                        + " AND o.UUID_CTR = :NEW.uuid "
                        + ") LOOP BEGIN "
                    + " IF i.guid IS NULL THEN raise_application_error (-20000, 'Протокол общего собрания собственников не отправлен в ГИС ЖКХ (№'||i.no||' от '||i.dt||'). Отправьте сначала в ГИС ЖКХ информацию о проведении Общего собрания собственников'); END IF; "
                    + " cnt:=cnt+1; "
                    + " END; END LOOP; "                            
                            
                    + " IF cnt=0 THEN BEGIN "
                    + "   SELECT COUNT(*) INTO cnt FROM tb_pp_ctr_files WHERE id_type=" + VocPublicPropertyContractFileType.i.VOTING_PROTO + " AND id_status=1 AND UUID_CTR=:NEW.uuid; "
                    + "   IF cnt=0 THEN raise_application_error (-20000, 'Не приведён протокол общего собрания собственников. Операция отменена.'); END IF; "
                    + " END; END IF; "
                            
                + "END; END IF; "
                            
                + "IF "
                    + "     :OLD.ID_CTR_STATUS = " + VocGisStatus.i.MUTATING.getId ()
                    + " AND :NEW.ID_CTR_STATUS = " + VocGisStatus.i.PENDING_RQ_PLACING.getId ()
                + " THEN "
                    + " :NEW.ID_CTR_STATUS := " + VocGisStatus.i.PENDING_RQ_EDIT.getId ()
                + "; END IF; "                            
                        
            + "END;"
                
        );

        trigger ("BEFORE INSERT OR UPDATE", ""

            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "                    

            + "IF :NEW.is_deleted = 0 AND :NEW.is_annuled = 0 THEN "

                + " FOR i IN ("
                    + "SELECT "
                    + " b.label address "
                    + "FROM "
                    + " tb_pp_ctr o "
                    + " INNER JOIN vc_buildings b ON o.FIASHOUSEGUID = b.houseguid "
                    + "WHERE o.is_deleted = 0"
                    + " AND o.is_annuled = 0"
                    + " AND o.FIASHOUSEGUID = :NEW.FIASHOUSEGUID "
                    + " AND o.CONTRACTNUMBER = :NEW.CONTRACTNUMBER "
                    + " AND o.uuid <> NVL(:NEW.uuid, '00') "
                    + ") LOOP"
                + " raise_application_error (-20000, "
                    + "'Договор с указанным номером (' "
                    + "|| :NEW.CONTRACTNUMBER"
                    + "||') по адресу '"
                    + "|| i.address"
                    + "|| ' уже существует. Операция отменена.'); "
                + " END LOOP; "

            + "END IF; "
            + "END; "

        );                
                
    }
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
        EDITING     (VocGisStatus.i.PENDING_RP_EDIT,      VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_STATE),
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
                case PENDING_RQ_EDIT:      return EDITING;
                case PENDING_RQ_ANNULMENT: return ANNULMENT;
                
                case PENDING_RP_PLACING:   return PLACING;
                case PENDING_RP_EDIT:      return EDITING;
                case PENDING_RP_ANNULMENT: return ANNULMENT;
                
                default: return null;
                
            }            
            
        }
/*        
        public static Action forLogAction (VocAction.i a) {
            switch (a) {
                case APPROVE: return PLACING;
                case ANNUL:   return ANNULMENT;
                default: return null;
            }            
        }
*/
    };
        
}