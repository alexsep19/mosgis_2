package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.NUMERIC;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class WorkingListItem extends EnTable {

    public enum c implements EnColEnum {

        UUID_WORKING_LIST    (WorkingList.class,      "Ссылка на перечень"),
        UUID_ORG_WORK        (OrganizationWork.class, "Ссылка на работу/услугу организации"),
        
//        ID_LOG               (WorkingListLog.class,  "Последнее событие редактирования"),
        INDEX_               (NUMERIC,  4, 0, null,   "Номер строки в перечне работ и услуг"),
        
        PRICE                (NUMERIC, 14, 4, null,   "Цена"),
        AMOUNT               (NUMERIC, 14, 3, null,   "Объём"),
        COUNT                (NUMERIC,  4, 0, null,   "Количество"),
        TOTALCOST            (NUMERIC, 22, 2, null,   "Общая стоимость"),
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
                case UUID_WORKING_LIST:
                    return false;
                default: 
                    return true;
            }
        }

    }

    public WorkingListItem () {

        super ("tb_work_list_items", "Перечни работ и услуг на период");

        cols   (c.class);

        key    ("ndx", c.UUID_WORKING_LIST, c.INDEX_);
/*        
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