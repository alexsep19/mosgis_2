package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class InfrastructureLog extends LogTable {
    
    public InfrastructureLog () {
        
        super ("tb_infrastructures__log", "История редактирования объектов коммунальной инфраструктуры", Infrastructure.class, EnTable.c.class, Infrastructure.c.class);
        
    }
    
}
