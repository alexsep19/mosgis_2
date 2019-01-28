package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class AgreementPayment extends EnTable {

    public enum c implements EnColEnum {

        UUID_CTR             (PublicPropertyContract.class, "Ссылка на договор на пользования общим имуществом"),       
        
        ID_AP_STATUS          (VocGisStatus.class,        VocGisStatus.i.PROJECT.asDef (),     "Статус с точки зрения mosgis"),
        ID_AP_STATUS_GIS      (VocGisStatus.class,        VocGisStatus.i.PROJECT.asDef (),     "Статус с точки зрения ГИС ЖКХ"),

        ID_LOG                (AgreementPaymentLog.class,  "Последнее событие редактирования"),
        
        DATEFROM              (Type.DATE,                      "Начало периода оплаты"),
        DATETO                (Type.DATE,                      "Окончание периода оплаты"),

        BILL                  (Type.NUMERIC, 10, 2, null,      "Начислено за период"),
        DEBT                  (Type.NUMERIC, 10, 2, null,      "Размер задолженности (-)/переплаты (+) за период"),
        PAID                  (Type.NUMERIC, 10, 2, null,      "Оплачено за период"),
        
        REASONOFANNULMENT     (Type.STRING,  1000,  null,       "Причина аннулирования"),
        IS_ANNULED            (Type.BOOLEAN,        new Virt ("DECODE(\"REASONOFANNULMENT\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0"),
        
        AGREEMENTPAYMENTVERSIONGUID  (Type.UUID, null, "Идентификатор версии сведений о внесении платы в ГИС ЖКХ")

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
                case UUID_CTR:
                    return false;
                default: 
                    return true;
            }
        }

    }

    public AgreementPayment () {        
        super ("tb_pp_ctr_ap", "Плата по договорам на пользование общим имуществом");
        cols   (c.class);        
        key    ("uuid_org", c.UUID_CTR);        
        
        trigger ("BEFORE UPDATE", 

            "BEGIN "

                + "IF :NEW.ID_AP_STATUS <> :OLD.ID_AP_STATUS "
                    + " AND :OLD.ID_AP_STATUS <> " + VocGisStatus.i.FAILED_PLACING.getId ()
                    + " AND :NEW.ID_AP_STATUS =  " + VocGisStatus.i.PROJECT.getId ()
                + " THEN "
                    + " :NEW.ID_AP_STATUS := " + VocGisStatus.i.MUTATING.getId ()
                + "; END IF; "

            + " END;"

        );

        trigger ("BEFORE INSERT OR UPDATE", ""
                
            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "                    

            + "IF :NEW.is_deleted = 0 AND :NEW.id_ap_status NOT IN (" + VocGisStatus.i.ANNUL + ") THEN BEGIN "

                + " FOR i IN ("
                    + "SELECT "
                    + " o.DATEFROM"
                    + " , o.DATETO "
                    + "FROM "
                    + " tb_pp_ctr_ap o "
                    + "WHERE o.is_deleted = 0"
                    + " AND o.id_ap_status NOT IN (" + VocGisStatus.i.ANNUL + ")"
                    + " AND o.UUID_CTR = :NEW.UUID_CTR "
                    + " AND o.DATETO   >= :NEW.DATEFROM "
                    + " AND o.DATEFROM <= :NEW.DATETO "
                    + " AND o.uuid <> NVL(:NEW.uuid, '00') "
                    + ") LOOP"
                + " raise_application_error (-20000, "
                    + "'Указанный период пересекается с другой информацией об оплате по данному договору с ' "
                    + "|| TO_CHAR (i.DATEFROM, 'DD.MM.YYYY')"
                    + "||' по '"
                    + "|| TO_CHAR (i.DATETO, 'DD.MM.YYYY')"
                    + "|| '. Операция отменена.'); "
                + " END LOOP; "

            + "END; END IF; "
                    
        + "END;");        
        
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