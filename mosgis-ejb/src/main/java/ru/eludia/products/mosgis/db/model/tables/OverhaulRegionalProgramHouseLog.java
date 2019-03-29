package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class OverhaulRegionalProgramHouseLog extends LogTable {
    
    public OverhaulRegionalProgramHouseLog () {
        
        super ("tb_oh_reg_pr_houses__log", "",
                OverhaulRegionalProgramHouse.class,
                EnTable.c.class,
                OverhaulRegionalProgramHouse.c.class);
        
    }
    
}
