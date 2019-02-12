package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class AdditionalServiceLog extends LogTable {
    
    public AdditionalServiceLog () {

        super ("tb_add_services__log", "Дополнительные услуги: история изменения", AdditionalService.class
            , EnTable.c.class
            , AdditionalService.c.class
        );

        trigger ("AFTER INSERT", "BEGIN "
            + " RETURN;"
        + "END;");
        
    }
    
}