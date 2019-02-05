package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.INTEGER;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class Infrastructure extends EnTable {
    
    public enum c implements EnColEnum {
        
        ID_IS_STATUS            (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус объекта инфраструктуры с точки зрения mosgis"),
        ID_IS_STATUS_GIS        (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус объекта инфраструктуры с точки зрения ГИС ЖКХ"),
        
        NAME                    (STRING, 140, "Наименование объекта"),
        
        CODE_VC_NSI_39          (STRING,  20, "Основание управления (НСИ 39)"),
        INDEFINITEMANAGEMENT    (BOOLEAN,     "Бессрочное управление"),
        ENDMANAGMENTDATE        (DATE,  null, "Окончание управления"),
        
        MANAGEROKI              (VocOrganization.class,       "Правообладатель"),
        MANAGEROKI_LABEL        (STRING,                null, "Нименование правообладателя"),
        
        CODE_VC_NSI_33          (STRING, 20, "Вид объекта (НСИ 33)"),
        
        INDEPENDENTSOURCE       (BOOLEAN,     null, "Признак автономного источника снабжения"),
        CODE_VC_NSI_34          (STRING,  20, null, "Вид водозаборного сооружения (НСИ 34)"),
        CODE_VC_NSI_35          (STRING,  20, null, "Тип газораспределительной сети (НСИ 35)"),
        CODE_VC_NSI_40          (STRING,  20, null, "Вид топлива (НСИ 40)"),
        CODE_VC_NSI_37          (STRING,  20, null, "Тип электрической подстанции (НСИ 37)"),
        CODE_VC_NSI_38          (STRING,  20, null, "Вид электростанции (НСИ 38)"),
        
        OKTMO                   (VocOktmo.class,      "ОКТМО"),
        OKTMO_CODE              (STRING,  11,   null, "Код ОКТМО"),
        ADRESS                  (STRING, 140,   null, "Адрес объекта"),
        COMISSIONINGYEAR        (NUMERIC,  4,   null, "Год ввода в эксплуатацию"),
        COUNTACCIDENTS          (INTEGER,       null, "Число аварий на 100 км сетей"),
        DETERIORATION           (NUMERIC, 3, 1, null, "Уровень износа (%)"),
        
        ADDINFO                 (STRING, 2000, null, "Дополнительная информация"),
        
        ID_LOG                  (InfrastructureLog.class, "Последнее событие редактирования")
        
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
                    return false;
                default:
                    return true;
            }
        }
        
    }
    
    public Infrastructure () {
        
        super ("tb_infrastructures", "Объекты коммунальной инфраструктуры");
        
        cols (c.class);
        
        trigger ("BEFORE INSERT OR UPDATE", ""
                + "BEGIN "
                    + "IF :NEW.manageroki IS NOT NULL THEN "
                        + "SELECT org.label "
                        + "INTO :NEW.manageroki_label "
                        + "FROM vc_orgs org "
                        + "WHERE org.orgrootentityguid = :NEW.manageroki; "
                    + "END IF; "
                    + "IF :NEW.oktmo IS NOT NULL THEN "
                        + "SELECT oktmo.code "
                        + "INTO :NEW.oktmo_code "
                        + "FROM vc_oktmo oktmo "
                        + "WHERE oktmo.id = :NEW.oktmo; "
                    + "END IF; "
                + "END; "
        );
        
        trigger ("BEFORE UPDATE", ""
                + "DECLARE "
                    + "cnt NUMBER; "
                + "BEGIN "
                    + "IF :NEW.ID_IS_STATUS <> :OLD.ID_IS_STATUS "
                        + " AND :OLD.ID_IS_STATUS <> " + VocGisStatus.i.FAILED_PLACING.getId ()
                        + " AND :NEW.ID_IS_STATUS =  " + VocGisStatus.i.PROJECT.getId ()
                    + " THEN "
                        + " :NEW.ID_IS_STATUS := " + VocGisStatus.i.MUTATING.getId ()
                    + "; END IF; "
                
                    + "IF :NEW.is_deleted=0 AND :NEW.ID_IS_STATUS <> :OLD.ID_IS_STATUS AND :NEW.ID_IS_STATUS=" + VocGisStatus.i.PENDING_RQ_PLACING.getId () + " THEN BEGIN "
                            
                        + "SELECT COUNT(*) INTO cnt FROM vw_nsi_33_objects code_objects WHERE code_objects.code = :NEW.CODE_VC_NSI_33; "
                        + "IF cnt > 0 THEN  BEGIN "
                            + "SELECT COUNT(*) INTO cnt FROM tb_oki_resources res WHERE res.uuid_oki = :NEW.uuid; "
                            + "IF cnt = 0 THEN "
                                + "raise_application_error (-20000, 'Укажите хотя бы одну характеристику мощностей объекта. Операция отменена'); "
                            + "END IF; END; "
                        + "ELSE BEGIN "
                            + "SELECT COUNT(*) INTO cnt FROM tb_oki_tr_resources res WHERE res.uuid_oki = :NEW.uuid; "
                            + "IF cnt = 0 THEN "
                                + "raise_application_error (-20000, 'Укажите хотя бы одну характеристику передачи коммунальных ресурсов. Операция отменена'); "
                            + "END IF; "
                            + "SELECT COUNT(*) INTO cnt FROM tb_oki_net_pieces net WHERE net.uuid_oki = :NEW.uuid; "
                            + "IF cnt = 0 THEN "
                                + "raise_application_error (-20000, 'Укажите хотя бы один участок сети. Операция отменена'); "
                            + "END IF; "
                        + "END; END IF; "
                
                        + "IF :NEW.indefinitemanagement=0 AND :NEW.endmanagmentdate IS NULL THEN "
                            + "raise_application_error (-20000, 'Не указана дата окончания управления. Операция отменена'); "
                        + "END IF; "
                            
                        + "IF :NEW.comissioningyear IS NULL THEN "
                            + "raise_application_error (-20000, 'Не указан год ввода объекта в эксплуатацию. Операция отменена'); "
                        + "END IF; "
                            
                        + "IF :NEW.oktmo_code IS NULL THEN "
                            + "raise_application_error (-20000, 'Не указан ОКТМО объекта. Операция отменена'); "
                        + "END IF; "
                    + "END; END IF; "
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
