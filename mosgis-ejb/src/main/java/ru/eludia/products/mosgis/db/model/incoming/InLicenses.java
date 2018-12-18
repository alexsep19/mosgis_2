package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class InLicenses extends Table {

    public InLicenses () {
        
        super ("in_licenses", "Запросы на импорт лицензий по ОГРН");
        
        pk  ("uuid",     Type.UUID,             NEW_UUID, "Ключ");
        fk  ("uuid_org", VocOrganization.class,           "Организация");
        col ("ts",       Type.TIMESTAMP,        NOW,      "Дата/время записи в БД");
                        
    }
    
}