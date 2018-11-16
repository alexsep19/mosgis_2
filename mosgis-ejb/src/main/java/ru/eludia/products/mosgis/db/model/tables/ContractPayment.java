package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.NUMERIC;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocContractPaymentType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class ContractPayment extends EnTable {

    public enum c implements EnColEnum {

        UUID_CONTRACT        (Contract.class,          "Ссылка на договор"),
        UUID_CONTRACT_OBJECT (ContractObject.class,    "Ссылка на объект договора"),
        FIASHOUSEGUID        (VocBuilding.class,       "Дом"),
        ID_CTR_STATUS        (VocGisStatus.class,      VocGisStatus.DEFAULT, "Статус объекта договора с точки зрения mosgis"),
        ID_CTR_STATUS_GIS    (VocGisStatus.class,      VocGisStatus.DEFAULT, "Статус объекта договора с точки зрения ГИС ЖКХ"),        
        TYPE_                (VocContractPaymentType.class,    "Тип размера платы"),
         
//        ID_LOG               (ContractPaymentLog.class,  "Последнее событие редактирования"),
        BEGINDATE            (DATE,                  "Дата начала периода"),
        ENDDATE              (DATE,                  "Дата окончания периода"),
        HOUSEMANAGEMENTPAYMENTSIZE (NUMERIC, 10, 2, null,  "Размер платы (цена) за услуги, работы по управлению МКД (если утверждена протоколом обшего собрания собственников)/Размер платы за содержание жилого помещения, установленный по результатам открытого конкурса (если утверждена протоколом открытого конкурса)")

        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
//                case ID_LOG:
                case UUID_CONTRACT:
                case UUID_CONTRACT_OBJECT:
                case FIASHOUSEGUID:
                    return false;
                default: 
                    return true;
            }
        }

    }

    public ContractPayment () {
        
        super ("tb_ctr_payments", "[Сведения о размере платы за] услуги управления");

        cols   (c.class);
        
        key    ("uuid_contract", c.UUID_CONTRACT);
        key    ("uuid_contract_object", c.UUID_CONTRACT_OBJECT);

        trigger ("BEFORE INSERT", 
                
              "BEGIN "
                + "SELECT uuid_contract, fiashouseguid INTO :NEW.uuid_contract, :NEW.fiashouseguid FROM tb_contract_objects WHERE uuid=:NEW.uuid_contract_object; "                      
            + "END;"
                
        );
        
        /*
        
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
*/
    }

}