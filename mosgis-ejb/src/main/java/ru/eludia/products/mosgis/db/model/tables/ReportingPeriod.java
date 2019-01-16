package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;

public class ReportingPeriod extends EnTable {

    public enum c implements ColEnum {

        UUID_WORKING_PLAN      (WorkingPlan.class,           "Ссылка на план"),
        MONTH                  (Type.NUMERIC, 2,             "Месяц"),
        REPORTINGPERIODGUID    (Type.UUID,  null,            "Ссылка на период отчётности о выполненных работах")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public ReportingPeriod () {
        
        super  ("tb_reporting_periods", "Периоды отчётности в планах работ и услуг");
        
        cols   (c.class);        
        
        unique ("uuid_working_plan", 
            c.UUID_WORKING_PLAN, 
            c.MONTH
        );
        
        trigger ("AFTER INSERT OR UPDATE", ""

            + "BEGIN "

                + "UPDATE tb_work_plan_items "
                + " SET uuid_reporting_period = :NEW.uuid "
                + "WHERE UUID_REPORTING_PERIOD IS NULL"
                + " AND uuid_working_plan=:NEW.uuid_working_plan "
                + " AND month=:NEW.month; "

            + "END;"                

        );

    }

}