package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class ReportingPeriod extends EnTable {

    public enum c implements EnColEnum {

        UUID_WORKING_PLAN      (WorkingPlan.class,                           "Ссылка на план"),
        MONTH                  (Type.NUMERIC, 2,                             "Месяц"),
        REPORTINGPERIODGUID    (Type.UUID,  null,                            "Ссылка на период отчётности о выполненных работах"),
        ID_LOG                 (ReportingPeriodLog.class,                    "Последнее событие редактирования"),
        ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),
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
                case UUID_WORKING_PLAN:
                case MONTH:
                    return false;
                default: 
                    return true;
            }
        }        

    }

    public ReportingPeriod () {
        
        super  ("tb_reporting_periods", "Периоды отчётности в планах работ и услуг");
        
        cols   (c.class);        
        
        unique ("uuid_working_plan", 
            c.UUID_WORKING_PLAN, 
            c.MONTH
        );
        
        trigger ("AFTER INSERT OR UPDATE", ""

            + "BEGIN "

                + "UPDATE tb_work_plan_items "
                + " SET uuid_reporting_period = :NEW.uuid "
                + "WHERE UUID_REPORTING_PERIOD IS NULL"
                + " AND uuid_working_plan=:NEW.uuid_working_plan "
                + " AND month=:NEW.month; "

            + "END;"                

        );

    }
    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.PENDING_RQ_REFRESH, VocGisStatus.i.FAILED_PLACING),
//        REFRESHING  (VocGisStatus.i.PENDING_RP_REFRESH,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
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
//                case PENDING_RQ_REFRESH:   return REFRESHING;
                default: return null;
            }            
        }

        public static Action forLogAction (VocAction.i a) {
            switch (a) {
                case APPROVE: return PLACING;
//              case REFRESH: return REFRESHING;
                default: return null;
            }
        }

    };    

}