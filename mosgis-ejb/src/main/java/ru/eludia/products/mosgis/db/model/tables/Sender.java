package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import static ru.eludia.base.model.def.Def.*;

public class Sender extends Table {

    public Sender () {
        
        super ("tb_senders", "Поставщики данных");
        
        pk    ("uuid",         Type.UUID,     NEW_UUID, "Ключ");
        
        col   ("label",        Type.STRING,             "Наименование");
        
        item  (
            "uuid",  "00000000-0000-0000-0000-000000000000",
            "label", "Тестовый поставщик"
        );

    }
    
}