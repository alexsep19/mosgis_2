package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Num;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocHouseStatus;

public class WorkingList extends EnTable {

    public enum c implements EnColEnum {

        UUID_CONTRACT_OBJECT (ContractObject.class,  "Ссылка на объект договора"),
        UUID_CHARTER_OBJECT  (CharterObject.class,   "Ссылка на объект устава"),
        FIASHOUSEGUID        (VocBuilding.class,     "Дом"),
        ID_CTR_STATUS        (VocGisStatus.class,    new Num (VocGisStatus.i.PROJECT.getId ()), "Статус объекта договора с точки зрения mosgis"),
        ID_CTR_STATUS_GIS    (VocGisStatus.class,    new Num (VocGisStatus.i.PROJECT.getId ()), "Статус объекта договора с точки зрения ГИС ЖКХ"),
        ID_LOG               (WorkingListLog.class,  "Последнее событие редактирования"),
        DT_FROM              (Type.DATE,                  "Период \"с\"  (первое число первого месяца периода)"),
        DT_TO                (Type.DATE,                  "Период \"по\" (последнее число последнего месяца периода)"),
        COUNT                (Type.NUMERIC,  4, 0, null,  "Количество"),
        TOTALCOST            (Type.NUMERIC, 22, 2, null,  "Общая стоимость"),        
        WORKLISTGUID         (Type.UUID, null,            "Идентификатор перечня")        
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
                case UUID_CONTRACT_OBJECT:
                case UUID_CHARTER_OBJECT:
                case FIASHOUSEGUID:
                    return false;
                default: 
                    return true;
            }
        }

    }

    public WorkingList () {

        super ("tb_work_lists", "Перечни работ и услуг на период");

        cols   (c.class);

        key    ("fiashouseguid", c.FIASHOUSEGUID);                
        key    ("uuid_contract_object", c.UUID_CONTRACT_OBJECT);                
        key    ("uuid_charter_object", c.UUID_CHARTER_OBJECT);                
        
        trigger ("BEFORE INSERT", 
                
              "BEGIN"

            + " IF :NEW.uuid_contract_object IS NOT NULL THEN "
                + "SELECT fiashouseguid INTO :NEW.fiashouseguid FROM tb_contract_objects WHERE uuid=:NEW.uuid_contract_object; "
            + " END IF;"
                      
            + " IF :NEW.uuid_charter_object IS NOT NULL THEN "
                + "SELECT fiashouseguid INTO :NEW.fiashouseguid FROM tb_charter_objects WHERE uuid=:NEW.uuid_charter_object; "
            + " END IF;"
                      
            + "END;"
                
        );
                
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

                + "IF :NEW.is_deleted=0 AND :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS AND :NEW.ID_CTR_STATUS=" + VocGisStatus.i.PENDING_RQ_PLACING.getId () + " THEN BEGIN "
                
                    + " SELECT COUNT(*) INTO cnt FROM tb_houses WHERE id_status=" + VocHouseStatus.i.PUBLISHED + " AND fiashouseguid=:NEW.fiashouseguid; "
                    + " IF cnt=0 THEN raise_application_error (-20000, 'Первоначально необходимо разместить паспорт дома в ГИС ЖКХ'); END IF; "

                    + " SELECT COUNT(*) INTO cnt FROM tb_work_list_items WHERE is_deleted=0 AND UUID_WORKING_LIST=:NEW.uuid; "
                    + " IF cnt=0 THEN raise_application_error (-20000, 'Перечень не содержит ни одной работы/услуги. Операция отменена.'); END IF; "

                + "END; END IF; "
                        
            + "END;"
                
        );
        
        trigger ("BEFORE INSERT OR UPDATE", ""
                
            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "                    
                    
            + "IF :NEW.is_deleted = 0 THEN "

                + "IF :NEW.uuid_contract_object IS NOT NULL THEN "
                    + " FOR i IN ("
                        + "SELECT "
                        + " o.dt_from"
                        + " , o.dt_to "
                        + "FROM "
                        + " tb_work_lists o "
                        + "WHERE o.is_deleted = 0"
                        + " AND o.uuid_contract_object = :NEW.uuid_contract_object "
                        + " AND o.dt_to   >= :NEW.dt_to "
                        + " AND o.dt_from <= :NEW.dt_from "
                        + " AND o.uuid <> NVL(:NEW.uuid, '00') "
                        + ") LOOP"
                    + " raise_application_error (-20000, "
                        + "'Для этого объекта уже зарегистртрован перечень работ с ' "
                        + "|| TO_CHAR (i.dt_from, 'DD.MM.YYYY')"
                        + "||' по '"
                        + "|| TO_CHAR (i.dt_to, 'DD.MM.YYYY')"
                        + "|| '. Операция отменена.'); "
                    + " END LOOP; "
                + "END IF; "                                        
                
                + "IF :NEW.uuid_charter_object IS NOT NULL THEN "
                    + " FOR i IN ("
                        + "SELECT "
                        + " o.dt_from"
                        + " , o.dt_to "
                        + "FROM "
                        + " tb_work_lists o "
                        + "WHERE o.is_deleted = 0"
                        + " AND o.uuid_charter_object = :NEW.uuid_charter_object "
                        + " AND o.dt_to   >= :NEW.dt_to "
                        + " AND o.dt_from <= :NEW.dt_from "
                        + " AND o.uuid <> NVL(:NEW.uuid, '00') "
                        + ") LOOP"
                    + " raise_application_error (-20000, "
                        + "'Для этого объекта уже зарегистртрован перечень работ с ' "
                        + "|| TO_CHAR (i.dt_from, 'DD.MM.YYYY')"
                        + "||' по '"
                        + "|| TO_CHAR (i.dt_to, 'DD.MM.YYYY')"
                        + "|| '. Операция отменена.'); "
                    + " END LOOP; "
                + "END IF; "                                        

            + "END IF; "                                        
                    
        + "END;");        

    }
    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.PENDING_RQ_REFRESH, VocGisStatus.i.FAILED_PLACING),
        REFRESHING  (VocGisStatus.i.PENDING_RP_REFRESH,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
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
                case PENDING_RQ_REFRESH:   return REFRESHING;
                default: return null;
            }            
        }

        public static Action forLogAction (VocAction.i a) {
            switch (a) {
                case APPROVE: return PLACING;
                case ANNUL:   return ANNULMENT;
                case REFRESH: return REFRESHING;
                default: return null;
            }
        }

    };

}