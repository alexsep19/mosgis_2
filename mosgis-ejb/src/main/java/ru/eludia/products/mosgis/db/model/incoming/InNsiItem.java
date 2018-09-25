package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NOW;

public class InNsiItem extends Table {

    public InNsiItem () {

        super ("in_nsi_items", "Импорты справочников НСИ");

        pk    ("uuid",              Type.UUID,              "Ключ");

        ref   ("uuid_in_nsi_group", InNsiGroup.class, null, "Импорт группы");

        col   ("ts",                Type.TIMESTAMP,   NOW,  "Дата/время записи в БД");
        col   ("registrynumber",    Type.INTEGER,           "Реестровый номер справочника");
        col   ("page",              Type.INTEGER,     null, "Номер страницы");

    }

}