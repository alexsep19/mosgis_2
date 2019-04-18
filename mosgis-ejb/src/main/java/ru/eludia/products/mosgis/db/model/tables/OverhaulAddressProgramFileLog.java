package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisFileLogTable;

public class OverhaulAddressProgramFileLog extends GisFileLogTable {
    
    public OverhaulAddressProgramFileLog () {
        
        super ("tb_oh_addr_pr_files__log", "История изменений файлов документов адресных программ капитального ремонта",
                OverhaulAddressProgramFile.class,
                EnTable.c.class,
                AttachTable.c.class,
                OverhaulAddressProgramFile.c.class
        );
        
    }

}
