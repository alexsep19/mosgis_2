package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class InfrastructureResourceLog extends LogTable {
    
    public InfrastructureResourceLog () {
        
        super ("tb_infrastructer_resources__log", "История изменений объектов мощностей ОКИ", InfrastructureResource.class, EnTable.c.class, InfrastructureResource.c.class);
        
    }
    
}
