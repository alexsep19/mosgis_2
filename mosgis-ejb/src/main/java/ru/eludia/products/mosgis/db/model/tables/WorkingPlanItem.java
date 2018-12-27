package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class WorkingPlanItem extends EnTable {

    public enum c implements EnColEnum {

        UUID_WORKING_PLAN      (WorkingPlan.class,           "Ссылка на план"),
        UUID_WORKING_LIST_ITEM (WorkingListItem.class,       "Ссылка на строку перечня"),
        MONTH                  (Type.NUMERIC, 2,             "Месяц"),
        WORKCOUNT              (Type.NUMERIC, 2,             "Количество работ"),
        DAYS_BITMASK           (Type.BINARY,  4,       null, "Битовая маска чисел месяца (например, 0x4001 — 31-е и 1-е числа)")
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
                case UUID_WORKING_PLAN:
                    return false;
                default: 
                    return true;
            }
        }

    }

    public WorkingPlanItem () {
        
        super  ("tb_work_plan_items", "Строки плана работ и услуг на период");        
        cols   (c.class);        
        unique ("uuid_working_plan", 
            c.UUID_WORKING_PLAN, 
            c.UUID_WORKING_LIST_ITEM,
            c.MONTH
        );
        
        trigger ("BEFORE UPDATE", ""
            + "BEGIN "
                + "IF :NEW.WORKCOUNT = 0 THEN :NEW.IS_DELETED := 0; END IF; "
            + "END;"                
        );
        
    }

}