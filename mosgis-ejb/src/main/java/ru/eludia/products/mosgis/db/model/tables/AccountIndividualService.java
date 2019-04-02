package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class AccountIndividualService extends AttachTable {

    public static final String TABLE_NAME = "tb_account_svc";

    public enum c implements EnColEnum {

        UUID_ACCOUNT                 (Account.class,                            "Лицевой счёт"),
        
        UUID_ADD_SERVICE             (AdditionalService.class,                  "Дополнительная услуга"),
        BEGINDATE                    (Type.DATE,                                "Дата начала представления услуги"),
        ENDDATE                      (Type.DATE,                                "Дата окончания представления услуги"),

        ID_LOG                       (AccountIndividualServiceLog.class,        "Последнее событие редактирования"),
        
        ID_CTR_STATUS                (VocGisStatus.class, VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS            (VocGisStatus.class, VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),
        
        ACCOUNTINDIVIDUALSERVICEGUID (Type.UUID,          null,                 "Идентификатор индивидуальной услуги ЛС"),
        
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {

            switch (this) {
                case UUID_ACCOUNT:
                case ID_LOG:
                    return false;
                default:
                    return true;                    
            }

        }

    }    

    public AccountIndividualService () {
        
        super  (TABLE_NAME, "Индивидуальные услуги лицевых счетов");
        
        cols   (c.class);
        
        key    ("parent", c.UUID_ACCOUNT);        
        key    ("attachmentguid", AttachTable.c.ATTACHMENTGUID);
       
        trigger ("BEFORE UPDATE", "BEGIN "
                
            + CHECK_LEN
                
            + "IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS "
                + " AND :OLD.ID_CTR_STATUS <> " + VocGisStatus.i.FAILED_PLACING
                + " AND :NEW.ID_CTR_STATUS =  " + VocGisStatus.i.PROJECT
            + " THEN "
                + " :NEW.ID_CTR_STATUS := " + VocGisStatus.i.MUTATING
            + "; END IF; "

            + "IF :NEW.ID_CTR_STATUS = " + VocGisStatus.i.ANNUL + " THEN " + " :NEW.ID_STATUS := " + VocFileStatus.i.DELETED + "; END IF; "
                    
        + "END;");        

    }
    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
        EDITING     (VocGisStatus.i.PENDING_RP_EDIT,      VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_STATE),
        ANNULMENT   (VocGisStatus.i.PENDING_RP_ANNULMENT, VocGisStatus.i.ANNUL,    VocGisStatus.i.FAILED_ANNULMENT),
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
                case PENDING_RQ_EDIT:      return EDITING;
                case PENDING_RP_EDIT:      return EDITING;
                case PENDING_RQ_ANNULMENT: return ANNULMENT;
                case PENDING_RP_ANNULMENT: return ANNULMENT;
                default: return null;
            }
            
        }

    };    
    
}