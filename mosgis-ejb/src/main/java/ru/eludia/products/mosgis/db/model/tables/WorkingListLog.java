package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class WorkingListLog extends LogTable {

    public WorkingListLog () {

        super ("tb_work_lists__log", "История редактирования перечней работ и услуг на период", WorkingList.class
            , EnTable.c.class
            , WorkingList.c.class
        );
        
    }
                
}