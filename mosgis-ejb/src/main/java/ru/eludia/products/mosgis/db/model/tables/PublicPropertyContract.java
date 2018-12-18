package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;


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