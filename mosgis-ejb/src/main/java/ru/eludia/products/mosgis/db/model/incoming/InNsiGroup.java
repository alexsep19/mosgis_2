package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocNsiListGroup;

public class InNsiGroup extends Table {

    public InNsiGroup () {
        
        super ("in_nsi_groups", "Импорты групп НСИ");
        
        pk    ("uuid",        Type.UUID, NEW_UUID, "Ключ");

        col   ("ts",          Type.TIMESTAMP, NOW, "Дата/время записи в БД");
        
        fk    ("listgroup",   VocNsiListGroup.class, "Группа");
                        
    }
    
}