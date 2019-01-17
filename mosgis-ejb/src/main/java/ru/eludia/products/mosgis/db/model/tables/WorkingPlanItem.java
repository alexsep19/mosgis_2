package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class WorkingPlanItem extends EnTable {

    public enum c implements EnColEnum {

        UUID_WORKING_PLAN      (WorkingPlan.class,           "Ссылка на план"),
        UUID_WORKING_LIST_ITEM (WorkingListItem.class,       "Ссылка на строку перечня"),
        UUID_REPORTING_PERIOD  (ReportingPeriod.class,       "Ссылка период отчётности"),
        MONTH                  (Type.NUMERIC, 2,             "Месяц"),
        WORKCOUNT              (Type.NUMERIC, 2,             "Количество работ"),
        DAYS_BITMASK           (Type.BINARY,  5,       null, "Битовая маска чисел месяца (например, 0x4001 — 31-е и 1-е числа)"),        
        WORKPLANITEMGUID       (Type.UUID,  null,            "Идентификатор работы/услуги перечня"),

        PRICE                  (Type.NUMERIC, 14, 4, null,   "Фактическая цена"),
        AMOUNT                 (Type.NUMERIC, 14, 3, null,   "Фактический объём"),
        TOTALCOST              (Type.NUMERIC, 22, 2, null,   "Фактическая стоимость выполненных работ"),
        
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
                + "IF :NEW.WORKCOUNT = 0 THEN :NEW.IS_DELETED := 1; ELSE :NEW.IS_DELETED := 0; END IF; "
            + "END;"                
        );
        
    }
    
    public static void addTo (DB db, Map<String, Object> r) throws SQLException {
        
        r.put ("items", db.getList (db.getModel ()
            .select (WorkingPlanItem.class, "*")               
            .where (WorkingPlanItem.c.UUID_WORKING_PLAN, r.get ("uuid_object"))
            .and (EnTable.c.IS_DELETED, 0)
            .toOne (WorkingListItem.class, "AS li", 
                WorkingListItem.c.WORKLISTITEMGUID.lc () + " AS worklistitemguid"
            ).on ()
        ));
        
    }

}