package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class AgreementPayment extends EnTable {

    public enum c implements EnColEnum {

        UUID_CTR             (PublicPropertyContract.class, "Ссылка на договор на пользования общим имуществом"),       
        
//        ID_CTR_STATUS        (VocGisStatus.class,        VocGisStatus.i.PROJECT.asDef (),     "Статус устава с точки зрения mosgis"),
//        ID_CTR_STATUS_GIS    (VocGisStatus.class,        VocGisStatus.i.PROJECT.asDef (),     "Статус устава с точки зрения ГИС ЖКХ"),
//        ID_CTR_STATE_GIS     (VocGisStatus.class,        VocGisStatus.i.NOT_RUNNING.asDef (), "Состояние устава с точки зрения ГИС ЖКХ"),

        ID_LOG                (AgreementPaymentLog.class,  "Последнее событие редактирования"),
        
        DATEFROM              (Type.DATE,                      "Начало периода оплаты"),
        DATETO                (Type.DATE,                      "Окончание периода оплаты"),

        BILL                  (Type.NUMERIC, 10, 2, null,      "Начислено за период"),
        DEBT                  (Type.NUMERIC, 10, 2, null,      "Размер задолженности (-)/переплаты (+) за период"),
        PAID                  (Type.NUMERIC, 10, 2, null,      "Оплачено за период")

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
    }
/*    
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
*/        
}