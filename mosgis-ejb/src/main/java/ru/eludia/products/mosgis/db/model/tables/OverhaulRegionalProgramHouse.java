package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class OverhaulRegionalProgramHouse extends EnTable {
    
    public enum c implements EnColEnum {
        
        ID_ORPH_STATUS       (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус записи с точки зрения mosgis"),
        ID_ORPH_STATUS_GIS   (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус записи с точки зрения ГИС ЖКХ"),
        
        PROGRAM_UUID         (OverhaulRegionalProgram.class, "Региональная программа"),
        
        HOUSE                (House.class, "Дом (МКД)"),
        
        ID_LOG               (OverhaulRegionalProgramHouseLog.class, "Последнее событие редактирования")
        
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
    
    public OverhaulRegionalProgramHouse () {
        
        super ("tb_oh_reg_pr_houses", "Дома региональной программы капитального ремонта");
        
        cols  (c.class);
        
        trigger ("BEFORE INSERT", ""
                + "DECLARE "
                    + "PRAGMA AUTONOMOUS_TRANSACTION; "
                    + "cnt NUMBER; "
                + "BEGIN "
                    + "SELECT COUNT(*) INTO cnt FROM tb_oh_reg_pr_houses houses WHERE houses.program_uuid = :NEW.program_uuid AND houses.house = :NEW.house AND :NEW.is_deleted = 0; "
                    + "IF cnt > 0 THEN "
                        + "raise_application_error (-20000, 'Данный дом уже включен в региональную программу'); "
                    + "END IF; "
                + "END; "
        );
        
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
