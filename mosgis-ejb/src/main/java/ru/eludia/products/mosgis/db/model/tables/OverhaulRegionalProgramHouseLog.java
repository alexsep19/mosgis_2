package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class OverhaulRegionalProgramHouseLog extends GisWsLogTable {
    
    public OverhaulRegionalProgramHouseLog () {
        
        super ("tb_oh_reg_pr_houses__log", "",
                OverhaulRegionalProgramHouse.class,
                EnTable.c.class,
                OverhaulRegionalProgramHouse.c.class);
        
    }
    
}
