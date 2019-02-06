package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.def.Bool.FALSE;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAccountType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;

public class Account extends EnTable {

    public enum c implements EnColEnum {
        
        UUID_ORG               (VocOrganization.class, null, "Организация, которая создала данный счёт"),
        ID_TYPE                (VocAccountType.class,       "Тип ЛС"),
        
	UUID_CONTRACT          (Contract.class,      null,  "Ссылка на договор"),
        UUID_CHARTER           (Charter.class,       null,  "Ссылка на устав"),
        
        ACCOUNTNUMBER          (Type.STRING,  30,    null,  "Причина закрытия (НСИ 22)"),
        
        LIVINGPERSONSNUMBER    (Type.NUMERIC, 4,     null,  "Количество проживающих"),
        TOTALSQUARE            (Type.NUMERIC, 25, 4, null,  "Общая площадь жилого помещения"),
        RESIDENTIALSQUARE      (Type.NUMERIC, 25, 4, null,  "Жилая площадь"),
        HEATEDAREA             (Type.NUMERIC, 25, 4, null,  "Отапливаемая площадь"),
        
        CODE_VC_NSI_22         (Type.STRING,  20,    null,                                  "Причина закрытия (НСИ 22)"),
        CLOSEREASON            (Type.STRING,  20,    new Virt  ("''||\"CODE_VC_NSI_22\""),  "Причина закрытия (НСИ 22)"),
        CLOSEDATE              (Type.DATE,           null,                                  "Дата закрытия"),

        DESCRIPTION            (Type.STRING,  250,   null,                                  "Примечание (для закрытия)"),
/*        
	FIASHOUSEGUID          (VocBuilding.class,   null,                "Глобальный уникальный идентификатор дома по ФИАС"),
	UUID_PREMISE           (Premise.class,       null,                "Помещение"),
	UUID_ROOM              (LivingRoom.class,    null,                "Комната"),
        SHAREPERCENT           (Type.NUMERIC, 5, 2, null,  "Отапливаемая площадь"),
*/        
        
        ISRENTER               (Type.BOOLEAN,          null,  "1, если является нанимателем; 0, если не является нанимателем"),
        ISACCOUNTSDIVIDED      (Type.BOOLEAN,          null,  "1, если лицевые счета на помещение(я) разделены; 0, если лицевые счета на помещение(я) не разделены"),

        IS_CUSTOMER_ORG        (Type.BOOLEAN,          Boolean.FALSE,  "1, если плательщик — юридическое лицо; 0, если физическое"),

        UUID_ORG_CUSTOMER      (VocOrganization.class, null, "Организация. ЮЛ/ИП/ОП"),
        UUID_PERSON_CUSTOMER   (VocPerson.class,       null, "Физическое лицо/индивидуальный предприниматель."),
        
        ACCOUNTGUID            (Type.UUID,             null,    "Идентификатор ЛС в ГИС ЖКХ (при обновлении данных ЛС)"),        

        ID_LOG                 (AccountLog.class,                    "Последнее событие редактирования"),
        
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
                case ID_TYPE:
                case IS_CUSTOMER_ORG:
                    return false;
                default:
                    return true;
            }
        }        

    }

    public Account () {
        
        super  ("tb_accounts", "Лицевые счета");
        
        cols   (c.class);        
/*                
        trigger ("AFTER INSERT OR UPDATE", ""

            + "BEGIN "

                + "UPDATE tb_work_plan_items "
                + " SET uuid_reporting_period = :NEW.uuid "
                + "WHERE UUID_REPORTING_PERIOD IS NULL"
                + " AND uuid_working_plan=:NEW.uuid_working_plan "
                + " AND month=:NEW.month; "

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
                        
                + " FOR i IN ("
                    + "SELECT "
                    + " o.uuid "
                    + "FROM "
                    + " tb_work_plan_items o "
                    + "WHERE o.is_deleted = 0"
                    + " AND o.uuid_reporting_period = :NEW.uuid "
                    + " AND (price IS NULL OR amount IS NULL OR count IS NULL) "
                    + ") LOOP"
                + " raise_application_error (-20000, 'Для всех работ по плану необходимо заполнить цену, объём и количество. Операция отменена.'); "
                + " END LOOP; "
                        
                        
                + "END; END IF; "

                + "IF "
                    + "     :OLD.ID_CTR_STATUS = " + VocGisStatus.i.MUTATING.getId ()
                    + " AND :NEW.ID_CTR_STATUS = " + VocGisStatus.i.PENDING_RQ_PLACING.getId ()
                + " THEN "
                    + " :NEW.ID_CTR_STATUS := " + VocGisStatus.i.PENDING_RQ_EDIT.getId ()
                + "; END IF; "
                        
            + "END;"
                
        );
*/
    }
/*    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
        EDITING     (VocGisStatus.i.PENDING_RP_EDIT,      VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_STATE),
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
                case PENDING_RQ_EDIT:      return EDITING;
                case PENDING_RP_PLACING:   return PLACING;
                case PENDING_RP_EDIT:      return EDITING;
                default: return null;
            }            
        }

    };    
*/
}