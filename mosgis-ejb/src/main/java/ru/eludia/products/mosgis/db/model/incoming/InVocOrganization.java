package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class InVocOrganization extends Table {

    public InVocOrganization () {
        
        super ("in_vc_orgs", "Запросы на импорт организаций по ОГРН");
        
        pk    ("uuid",        Type.UUID,             NEW_UUID,            "Ключ");
        
        col   ("ts",          Type.TIMESTAMP, NOW,  "Дата/время записи в БД");        
        fk    ("uuid_user",   VocUser.class,  null, "Оператор");
        
        col   ("ogrn",        Type.NUMERIC, 15,     "ОГРН");
                        
    }
    
}