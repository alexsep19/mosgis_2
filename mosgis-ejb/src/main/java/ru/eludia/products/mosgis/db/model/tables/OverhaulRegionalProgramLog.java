package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class OverhaulRegionalProgramLog extends GisWsLogTable {

    public OverhaulRegionalProgramLog () {
        
        super ("tb_oh_reg_programs__log", "Региональные программы капитального ремонта: история изменений",
                OverhaulRegionalProgram.class,
                EnTable.c.class,
                OverhaulRegionalProgram.c.class);
        
    }
    
}