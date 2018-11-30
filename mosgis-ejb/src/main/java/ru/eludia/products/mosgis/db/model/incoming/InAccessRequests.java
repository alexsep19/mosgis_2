package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;

public class InAccessRequests extends Table {

    public InAccessRequests () {        
        super ("in_acc_req", "Запросы на импорт прав делегирования");        
        pk    ("uuid",        Type.UUID,             NEW_UUID,            "Ключ");        
        col   ("ts",          Type.TIMESTAMP, NOW,  "Дата/время записи в БД");        
    }
    
}