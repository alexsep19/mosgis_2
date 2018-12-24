package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;
import ru.gosuslugi.dom.schema.integration.services.ImportWorkingListRequest;

public class WorkingListLog extends LogTable {

    public WorkingListLog () {

        super ("tb_work_lists__log", "История редактирования перечней работ и услуг на период", WorkingList.class
            , EnTable.c.class
            , WorkingList.c.class
        );
        
    }
    
    public static ImportWorkingListRequest toImportWorkingListRequest (Map<String, Object> r) {
        final ImportWorkingListRequest result = DB.to.javaBean (ImportWorkingListRequest.class, r);
        return result;
    }

    Select getForExport (String id) {
        
        return (Get) getModel ()
            .get (this, id, "*")
            .toOne (WorkingList.class, "AS r"
                , WorkingList.c.ID_CTR_STATUS.lc ()
            ).on ();
                
    }
                
}