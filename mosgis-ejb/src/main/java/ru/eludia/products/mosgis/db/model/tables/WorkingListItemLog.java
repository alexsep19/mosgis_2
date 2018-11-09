package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class WorkingListItemLog extends LogTable {

    public WorkingListItemLog () {

        super ("tb_work_list_items__log", "История редактирования строк перечней работ и услуг на период", WorkingListItem.class
            , EnTable.c.class
            , WorkingListItem.c.class
        );
        
    }
                
}