package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class AdditionalServiceLog extends GisWsLogTable {
    
    public AdditionalServiceLog () {

        super (AdditionalService.TABLE_NAME + "__log", "Дополнительные услуги: история изменения", AdditionalService.class
            , EnTable.c.class
            , AdditionalService.c.class
        );
        
        col   ("elementguid_new",  Type.UUID, null, "Идентификатор новой версии существующего (в ГИС) элемента справочника");

        trigger ("AFTER INSERT", "BEGIN "
            + " RETURN;"
        + "END;");
        
    }
    
}