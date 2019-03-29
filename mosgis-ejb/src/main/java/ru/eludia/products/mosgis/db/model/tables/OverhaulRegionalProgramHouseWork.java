package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;

public class OverhaulRegionalProgramHouseWork extends EnTable {
    
    public enum c implements EnColEnum {
        
        ID_ORPHW_STATUS       (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус вида работы капитального ремонта с точки зрения mosgis"),
        ID_ORPHW_STATUS_GIS   (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус вида работы капитального ремонта с точки зрения ГИС ЖКХ"),
        
        HOUSE_UUID            (OverhaulRegionalProgramHouse.class, "Дом, к которому привязан вид работы"),

        WORK                  (STRING, 20, "Вид работы (НСИ 219)"),
        
        STARTMONTH            (NUMERIC, 2, "Месяц начала периода"),
        STARTYEAR             (NUMERIC, 4, "Год начала периода"),
        
        STARTYEARMONTH        (DATE, new Virt ("TO_DATE(\"STARTYEAR\" || '-' || \"STARTMONTH\", 'YYYY-MM')"), "Дата начала периода (gYearMonth)"),
        
        ENDMONTH              (NUMERIC, 2, "Месяц окончания периода"),
        ENDYEAR               (NUMERIC, 4, "Год окончания периода"),
        
        ENDYEARMONTH          (DATE, new Virt ("TO_DATE(\"ENDYEAR\" || '-' || \"ENDMONTH\", 'YYYY-MM')"), "Дата окончания периода (gYearMonth)"),
        
        ID_LOG                (OverhaulRegionalProgramHouseWorkLog.class, "Последнее событие редактирования")
        
        ;
        
        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }
        
    }
    
    public OverhaulRegionalProgramHouseWork () {
        
        super ("tb_oh_reg_pr_house_work", "Вид работ по дому РПКР");
        
        cols  (c.class);
        
    }
    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING)
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
                default: return null;
            }            
        }

        public static Action forLogAction (VocAction.i a) {
            switch (a) {
                case APPROVE: return PLACING;
                default: return null;
            }
        }

    };
    
}
