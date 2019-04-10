package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class OverhaulShortProgramHouseWorkLog extends GisWsLogTable {

    public OverhaulShortProgramHouseWorkLog () {
        
        super ("tb_oh_shrt_pr_house_work__log", "Вид работы по дому КПР: история изменений",
                OverhaulShortProgramHouseWork.class,
                EnTable.c.class,
                OverhaulShortProgramHouseWork.c.class);
        
    }
    
}
