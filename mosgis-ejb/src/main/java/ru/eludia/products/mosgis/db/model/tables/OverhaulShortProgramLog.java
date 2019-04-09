package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

class OverhaulShortProgramLog extends GisWsLogTable {
    
    public OverhaulShortProgramLog () {
        
        super ("tb_oh_shrt_programs__log", "Краткосрочные программы капитального ремонта: история изменений",
                OverhaulShortProgram.class,
                EnTable.c.class,
                OverhaulShortProgram.c.class
        );
        
    }
    
}
