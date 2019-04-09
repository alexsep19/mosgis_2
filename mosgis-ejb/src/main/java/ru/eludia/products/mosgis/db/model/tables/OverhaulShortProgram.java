package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class OverhaulShortProgram extends EnTable {
    
    public enum c implements EnColEnum {
        
        LAST_SUCCESFULL_STATUS  (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Последний успешный статус обмена с ГИС"),
        ID_OSP_STATUS           (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус краткосрочной программы капитального ремонта с точки зрения mosgis"),
        ID_OSP_STATUS_GIS       (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус краткосрочной программы капитального ремонта с точки зрения ГИС ЖКХ"),
        
        ORG_UUID                (VocOrganization.class, null, "Поставщик информации"),
        
        PROGRAMNAME             (Type.STRING, 1000, "Наименование программы"),
        STARTYEAR               (Type.NUMERIC, 4, "Год начала периода реализации"),
        ENDYEAR                 (Type.NUMERIC, 4, "Год окончания периода реализации"),
        
        ID_LOG                  (OverhaulShortProgramLog.class, "Последнее событие редактирования"),
        
        PLANGUID                (Type.UUID,   null, "Идентификатор региональной программы"),
        UNIQUENUMBER            (Type.STRING, null, "Уникальный номер")
        
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
    
    public OverhaulShortProgram () {
        
        super ("tb_oh_shrt_programs", "Кратскосрочные программы капитального ремонта");
        
        cols  (c.class);
        
    }
    
    public enum Action {
        
        PROJECT_PUBLISH     (VocGisStatus.i.PENDING_RP_PUBLISHANDPROJECT, VocGisStatus.i.PROGRAM_WORKS_PLACE_INITIALIZED,          VocGisStatus.i.FAILED_PUBLISHANDPROJECT),
        PLACING             (VocGisStatus.i.PENDING_RP_PLACING,           VocGisStatus.i.APPROVED,                                 VocGisStatus.i.FAILED_PLACING),
        PROJECT_DELETE      (VocGisStatus.i.PENDING_RP_DELETEPROJECT,     VocGisStatus.i.PROJECT_DELETED,                          VocGisStatus.i.FAILED_DELETEPROJECT),
        ANNUL               (VocGisStatus.i.PENDING_RP_ANNULMENT,         VocGisStatus.i.ANNUL,                                    VocGisStatus.i.FAILED_ANNULMENT)
        
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
                case PENDING_RQ_PUBLISHANDPROJECT:
                    return PROJECT_PUBLISH;
                case PENDING_RQ_PLACING:
                    return PLACING;
                case PENDING_RQ_DELETEPROJECT:
                    return PROJECT_DELETE;
                case PENDING_RQ_ANNULMENT:
                    return ANNUL;
                default: return null;
            }            
        }

        public static Action forLogAction (VocAction.i a) {
            switch (a) {
                case PUBLISHANDPROJECT:
                    return PROJECT_PUBLISH;
                case APPROVE:
                    return PLACING;
                case DELETE_PROJECT:
                    return PROJECT_DELETE;
                case ANNUL:
                    return ANNUL;
                default: return null;
            }
        }

    };
    
}
