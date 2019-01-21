package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class InXlFileLog extends LogTable {

    public InXlFileLog () {
        
        super ("in_xl_files__log", "История импорта Excel", 
            InXlFile.class, 
            EnTable.c.class, 
            InXlFile.c.class
        );
        
    }
    
}
