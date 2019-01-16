package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class WorkingPlan extends EnTable {

    public enum c implements EnColEnum {

        UUID_WORKING_LIST    (WorkingList.class,     "Ссылка на перечень"),
        ID_CTR_STATUS        (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS    (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),
        ID_LOG               (WorkingPlanLog.class,  "Последнее событие редактирования"),
        YEAR                 (Type.NUMERIC, 4,       "Год")
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
                case UUID_WORKING_LIST:
                    return false;
                default: 
                    return true;
            }
        }

    }

    public WorkingPlan () {
        super ("tb_work_plans", "План работ и услуг на период");
        cols   (c.class);
        key    ("uuid_working_list", c.UUID_WORKING_LIST);
        
        trigger ("BEFORE UPDATE", 
                
            "DECLARE "
            + " cnt NUMBER;"
            + "BEGIN "

                + "IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS "
                    + " AND :OLD.ID_CTR_STATUS <> " + VocGisStatus.i.FAILED_PLACING.getId ()
                    + " AND :NEW.ID_CTR_STATUS =  " + VocGisStatus.i.PROJECT.getId ()
                + " THEN "
                    + " :NEW.ID_CTR_STATUS := " + VocGisStatus.i.MUTATING.getId ()
                + "; END IF; "
/*
                + "IF :NEW.is_deleted=0 AND :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS AND :NEW.ID_CTR_STATUS=" + VocGisStatus.i.PENDING_RQ_PLACING.getId () + " THEN BEGIN "
                
                    + " SELECT COUNT(*) INTO cnt FROM tb_houses WHERE id_status=" + VocHouseStatus.i.PUBLISHED + " AND fiashouseguid=:NEW.fiashouseguid; "
                    + " IF cnt=0 THEN raise_application_error (-20000, 'Первоначально необходимо разместить паспорт дома в ГИС ЖКХ'); END IF; "

                    + " SELECT COUNT(*) INTO cnt FROM tb_work_list_items WHERE is_deleted=0 AND UUID_WORKING_LIST=:NEW.uuid; "
                    + " IF cnt=0 THEN raise_application_error (-20000, 'Перечень не содержит ни одной работы/услуги. Операция отменена.'); END IF; "

                + "END; END IF; "
*/                        
            + "END;"
                
        );
        
    }
    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
//        ANNULMENT   (VocGisStatus.i.PENDING_RP_ANNULMENT, VocGisStatus.i.ANNUL,    VocGisStatus.i.FAILED_ANNULMENT)
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
//                case PENDING_RQ_ANNULMENT: return ANNULMENT;
                default: return null;
            }            
        }

        public static Action forLogAction (VocAction.i a) {
            switch (a) {
                case APPROVE: return PLACING;
//                case ANNUL:   return ANNULMENT;
                default: return null;
            }
        }

    };

}