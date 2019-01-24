package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisFileLogTable;

public class InfrastructureFileLog extends GisFileLogTable {
    
    public InfrastructureFileLog () {
        
        super ("tb_infrastructure_files__log", "История редактирования файлов, приложенных к ОКИ", InfrastructureFile.class
            , EnTable.c.class
            , AttachTable.c.class
            , InfrastructureFile.c.class
        );
    }
}
