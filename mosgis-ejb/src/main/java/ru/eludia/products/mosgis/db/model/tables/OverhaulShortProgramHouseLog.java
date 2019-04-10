package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class OverhaulShortProgramHouseLog extends GisWsLogTable {
    
    public OverhaulShortProgramHouseLog () {
        
        super ("tb_oh_shrt_pr_houses__log", "",
                OverhaulShortProgramHouse.class,
                EnTable.c.class,
                OverhaulShortProgramHouse.c.class);
        
    }
    
}
