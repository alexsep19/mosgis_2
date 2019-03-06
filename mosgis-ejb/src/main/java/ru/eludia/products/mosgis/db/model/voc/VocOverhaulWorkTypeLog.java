package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class VocOverhaulWorkTypeLog extends GisWsLogTable {
    
    public VocOverhaulWorkTypeLog () {
        
        super ("vc_oh_wk_types__log", "Виды работ капитального ремонта: история изменеий", 
                VocOverhaulWorkType.class, 
                EnTable.c.class, 
                VocOverhaulWorkType.c.class);
        
    }
    
}
