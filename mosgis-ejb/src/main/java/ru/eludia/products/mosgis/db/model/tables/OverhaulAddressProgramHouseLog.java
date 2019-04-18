package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class OverhaulAddressProgramHouseLog extends GisWsLogTable {
    
    public OverhaulAddressProgramHouseLog () {
        
        super ("tb_oh_addr_pr_houses__log", "",
                OverhaulAddressProgramHouse.class,
                EnTable.c.class,
                OverhaulAddressProgramHouse.c.class);
        
    }
    
}
