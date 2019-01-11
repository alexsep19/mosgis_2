package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class CheckPlanLog extends LogTable {

    public CheckPlanLog () {
        
        super ("tb_check_plans__log", "История редактирования объектов плановых проверок", CheckPlan.class, EnTable.c.class, CheckPlan.c.class);
        
    }
    
}
