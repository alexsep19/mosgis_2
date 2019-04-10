package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisFileLogTable;

public class OverhaulShortProgramFileLog extends GisFileLogTable {
    
    public OverhaulShortProgramFileLog () {
        
        super ("tb_oh_shrt_pr_files__log", "История изменений файлов документов краткосрочных программ капитального ремонта",
                OverhaulShortProgramFile.class,
                EnTable.c.class,
                AttachTable.c.class,
                OverhaulShortProgramFile.c.class
        );
        
    }

}
