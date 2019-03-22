package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisFileLogTable;

public class OverhaulRegionalProgramFileLog extends GisFileLogTable {
    
    public OverhaulRegionalProgramFileLog () {
        
        super ("tb_oh_reg_pr_files__log", "История изменений файлов документов региональных программ капитального ремонта",
                OverhaulRegionalProgramFile.class,
                EnTable.c.class,
                AttachTable.c.class,
                OverhaulRegionalProgramFile.c.class
        );
        
    }
    
}
