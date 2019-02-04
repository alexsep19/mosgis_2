package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.gosuslugi.dom.schema.integration.infrastructure.ImportOKIRequest;

public class InfrastructureLog extends GisWsLogTable {

    public InfrastructureLog () {
        
        super ("tb_infrastructures__log", "История редактирования объектов коммунальной инфраструктуры", Infrastructure.class, EnTable.c.class, Infrastructure.c.class);
        
    }
    
    public static ImportOKIRequest toImportOKIRequest (Map<String, Object> r) {
        final ImportOKIRequest result = new ImportOKIRequest ();
        return result;            
    }
    
}
