package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class BaseDecisionMSP extends EnTable {

    public static final String TABLE_NAME = "tb_base_dec_msp";

    public enum c implements EnColEnum {

        UUID_ORG                     (VocOrganization.class, null, "Организация, которая создала данную запись"),

        DECISIONNAME                 (Type.STRING,  500,    "Наименование основания принятия решения"),
        LABEL_UC                     (Type.STRING,          new Virt  ("UPPER(\"DECISIONNAME\")"), "НАИМЕНОВАНИЕ"),

        CODE_VC_NSI_301              (Type.STRING,  20,      "Тип решения о мерах социальной поддержки (НСИ 301)"),

	ISAPPLIEDTOSUBSIDIARIES      (Type.BOOLEAN, Bool.FALSE, "1, если применяется для субсидий, иначе 0"),
	ISAPPLIEDTOREFUNDOFCHARGES   (Type.BOOLEAN, Bool.FALSE, "1, если применяется для компенсации расходов, иначе 0"),

        ELEMENTGUID                  (Type.UUID,             null,  "Идентификатор существующего элемента справочника"),
        UNIQUENUMBER                 (Type.STRING,           null,  "Уникальный номер, присвоенный ГИС ЖКХ"),

        ID_LOG                       (BaseDecisionMSPLog.class, "Последнее событие редактирования"),

        ID_CTR_STATUS                (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS            (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {

            switch (this) {
                case UUID_ORG:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }

        }

    }

    public BaseDecisionMSP () {

        super (TABLE_NAME, "Основание принятия решения о мерах социальной поддержки гражданина (НСИ 302)");

        cols (c.class);

        key (c.UUID_ORG);

        trigger ("BEFORE INSERT OR UPDATE", ""

            + "BEGIN "

                + " IF :NEW.ID_CTR_STATUS_GIS = " + VocGisStatus.i.PROJECT + " THEN BEGIN "

                    + " IF :NEW.is_deleted=0 THEN :NEW.ID_CTR_STATUS := " + VocGisStatus.i.PENDING_RQ_PLACING
                    + " ; ELSE :NEW.ID_CTR_STATUS := " + VocGisStatus.i.PENDING_RQ_CANCEL
                    + " ; END IF;"

                + " END; END IF; "

                + " :NEW.ID_CTR_STATUS_GIS := " + VocGisStatus.i.PROJECT + "; "

            + "END;"

        );
    }

    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING, VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
        CANCEL      (VocGisStatus.i.PENDING_RP_CANCEL, VocGisStatus.i.CANCELLED, VocGisStatus.i.FAILED_CANCEL),
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
                case PENDING_RP_PLACING:   return PLACING;
                case PENDING_RQ_CANCEL:    return CANCEL;
                case PENDING_RP_CANCEL:    return CANCEL;
                default: return null;
            }            
        }
    };
}