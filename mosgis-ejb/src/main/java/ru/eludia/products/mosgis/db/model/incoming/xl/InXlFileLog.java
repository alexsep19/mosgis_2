package ru.eludia.products.mosgis.db.model.incoming.xl;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class InXlFileLog extends GisWsLogTable {

    public InXlFileLog () {
        
        super ("in_xl_files__log", "История импорта Excel", 
            InXlFile.class, 
            EnTable.c.class, 
            InXlFile.c.class
        );
        
    }
    
}
