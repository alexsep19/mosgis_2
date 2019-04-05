package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class OverhaulRegionalProgramHouseWorkLog extends GisWsLogTable {

    public OverhaulRegionalProgramHouseWorkLog () {
        
        super ("tb_oh_reg_pr_house_work__log", "Вид работы по дому РПКР: история изменений",
                OverhaulRegionalProgramHouseWork.class,
                EnTable.c.class,
                OverhaulRegionalProgramHouseWork.c.class);
        
    }
    
}