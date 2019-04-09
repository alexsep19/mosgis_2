package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class OverhaulShortProgramHouse extends EnTable {
    
    public enum c implements EnColEnum {
        
        ID_OSPH_STATUS       (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус записи с точки зрения mosgis"),
        ID_OSPH_STATUS_GIS   (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус записи с точки зрения ГИС ЖКХ"),
        
        PROGRAM_UUID         (OverhaulShortProgram.class, "Краткосрочная программа"),
        
        HOUSE                (House.class, "Дом (МКД)"),
        
        ID_LOG               (OverhaulShortProgramHouseLog.class, "Последнее событие редактирования")
        
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
    
    public OverhaulShortProgramHouse () {
        
        super ("tb_oh_shrt_pr_houses", "Дома краткосрочной программы капитального ремонта");
        
        cols  (c.class);
        
        trigger ("BEFORE INSERT", ""
                + "DECLARE "
                    + "PRAGMA AUTONOMOUS_TRANSACTION; "
                    + "cnt NUMBER; "
                + "BEGIN "
                    + "SELECT COUNT(*) INTO cnt FROM tb_oh_shrt_pr_houses houses WHERE houses.program_uuid = :NEW.program_uuid AND houses.house = :NEW.house AND houses.is_deleted = 0; "
                    + "IF cnt > 0 THEN "
                        + "raise_application_error (-20000, 'Данный дом уже включен в краткосрочную программу'); "
                    + "END IF; "
                + "END; "
        );
        
        trigger ("BEFORE UPDATE", ""
                + "DECLARE "
                    + "PRAGMA AUTONOMOUS_TRANSACTION; "
                    + "cnt NUMBER; "
                + "BEGIN "
                    + "IF (:OLD.is_deleted = 0 AND :NEW.is_deleted = 1) THEN "
                        + "SELECT COUNT (*) INTO cnt FROM tb_oh_shrt_pr_house_work works WHERE works.house_uuid = :OLD.uuid AND works.id_osphw_status = " + VocGisStatus.i.APPROVED.getId () + "; "
                        + "IF cnt > 0 THEN "
                            + "raise_application_error (-20000, 'Данный дом содержит как минимум один размещенный вид работ'); "
                        + "END IF; "
                    + "END IF; "
                + "END; "
        );
        
    }
    
}
