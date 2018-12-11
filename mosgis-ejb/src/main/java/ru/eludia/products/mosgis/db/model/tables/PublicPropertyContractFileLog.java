package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisFileLogTable;

public class PublicPropertyContractFileLog extends GisFileLogTable {
    
    public PublicPropertyContractFileLog () {
        
        super ("tb_pp_ctr_files__log", "История редактирования файлов, приложенных к к договорам на пользование общим имуществом", PublicPropertyContractFile.class
            , EnTable.c.class
            , AttachTable.c.class
            , PublicPropertyContractFile.c.class
        );
    }
}
