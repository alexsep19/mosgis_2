package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class OverhaulShortProgramHouseWork extends EnTable {
    
    public enum c implements EnColEnum {
        
        ID_OSPHW_STATUS       (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус вида работы капитального ремонта с точки зрения mosgis"),
        ID_OSPHW_STATUS_GIS   (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус вида работы капитального ремонта с точки зрения ГИС ЖКХ"),
        
        GUID                  (UUID, null, "Уникальный идентификатор в ГИС"),
        UNIQUENUMBER          (STRING, null, "Уникальный реестровый номер"),
        
        IMPORT_UUID           (OverhaulShortProgramHouseWorksImport.class, null, "Операция импорта, с которой связана запись"),
        IMPORT_ERR_TEXT       (STRING, null, "Текст ошибки импорта записи"),
        
        HOUSE_UUID            (OverhaulShortProgramHouse.class, "Дом, к которому привязан вид работы"),

        WORK                  (STRING, 20, "Вид работы (НСИ 219)"),
        
        ENDMONTH              (NUMERIC, 2, "Месяц окончания периода"),
        ENDYEAR               (NUMERIC, 4, "Год окончания периода"),
        
        ENDMONTHYEAR          (DATE, new Virt ("TO_DATE(\"ENDYEAR\" || '.' || \"ENDMONTH\", 'YYYY.MM')"), "Дата окончания периода (gYearMonth)"), // '.' used to avoid conflict with ENDYEARMONTH
        
        FUND                  (NUMERIC, 14, 2, "За счет средств Фонда содействия реформированию ЖКХ"),
        REGIONBUDGET          (NUMERIC, 14, 2, "За счет средств бюджета субъекта РФ"),
        MUNICIPALBUDGET       (NUMERIC, 14, 2, "За счет средств местного бюджета"),
        OWNERS                (NUMERIC, 14, 2, "За счет средств собственников"),
        
        TOTAL                 (NUMERIC, 14, 2, new Virt ("FUND+REGIONBUDGET+MUNICIPALBUDGET+OWNERS"), "Итого"),
        
        ID_LOG                (OverhaulShortProgramHouseWorkLog.class, "Последнее событие редактирования")
        
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
    
    public OverhaulShortProgramHouseWork () {
        
        super ("tb_oh_shrt_pr_house_work", "Вид работ по дому КПР");
        
        cols  (c.class);
        
        trigger ("BEFORE INSERT OR UPDATE", ""
                + "DECLARE "
                    + "PRAGMA AUTONOMOUS_TRANSACTION; "
                    + "cnt NUMBER; "
                + "BEGIN "
                    + "SELECT COUNT (*) INTO cnt FROM tb_oh_shrt_pr_house_work works WHERE "
                        + "works." + OverhaulShortProgramHouseWork.c.HOUSE_UUID.lc () + " = :NEW." + OverhaulShortProgramHouseWork.c.HOUSE_UUID.lc () + " AND "
                        + "works." + OverhaulShortProgramHouseWork.c.WORK.lc ()       + " = :NEW." + OverhaulShortProgramHouseWork.c.WORK.lc ()       + " AND "
                        + "works." + OverhaulShortProgramHouseWork.c.ENDMONTH.lc ()   + " = :NEW." + OverhaulShortProgramHouseWork.c.ENDMONTH.lc ()   + " AND "
                        + "works." + OverhaulShortProgramHouseWork.c.ENDYEAR.lc ()    + " = :NEW." + OverhaulShortProgramHouseWork.c.ENDYEAR.lc ()    + " AND "
                        + "works.uuid <> :NEW.uuid AND "
                        + "works.is_deleted = 0; "
                    + "IF cnt > 0 THEN "
                        + "raise_application_error (-20000, 'Данный вид работ с указанным периодом уже существует для данного дома'); "
                    + "END IF; "
                + "END; "
        );
        
    }
    
    public enum Action {
        
        ANNUL     (VocGisStatus.i.PENDING_RP_ANNULMENT,   VocGisStatus.i.ANNUL, VocGisStatus.i.FAILED_ANNULMENT)
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
                case PENDING_RQ_ANNULMENT:   return ANNUL;
                default: return null;
            }            
        }

        public static Action forLogAction (VocAction.i a) {
            switch (a) {
                case ANNUL: return ANNUL;
                default: return null;
            }
        }

    }
}