package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class InfrastructureTransportationResourceLog extends LogTable {
    
    public InfrastructureTransportationResourceLog () {
        
        super ("tb_oki_tr_resources__log", "История изменений объектов передачи коммунальных ресурсов ОКИ", 
                InfrastructureTransportationResource.class, EnTable.class, InfrastructureTransportationResource.c.class);
        
    }
    
}
