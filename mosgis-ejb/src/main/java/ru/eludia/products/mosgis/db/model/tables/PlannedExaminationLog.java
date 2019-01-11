package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class PlannedExaminationLog extends LogTable {

    public PlannedExaminationLog () {
        
        super ("tb_planned_examinations__log", "История редактирования объектов проверок", PlannedExamination.class, EnTable.c.class, PlannedExamination.c.class);
        
    }
    
}
