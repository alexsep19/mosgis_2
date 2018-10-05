package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState.i.PENDING;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.db.model.voc.VocAction;

public class AdditionalServiceLog extends Table {

    public AdditionalServiceLog () {

        super ("tb_add_services__log",                                                  "Дополнительные услуги: история изменения");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        ref   ("action",                    VocAction.class,                            "Действие");

        fk    ("uuid_object",               AdditionalService.class,                    "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,                 "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,                      null,   "Оператор");

        col   ("is_deleted",                Type.BOOLEAN,                       null,   "1, если запись удалена; иначе 0");                
        fk    ("okei",                      VocOkei.class,                      null,   "Единица измерения");
        col   ("additionalservicetypename", Type.STRING,           100,         null,   "Наименование вида дополнительной услуги");
        col   ("elementguid",               Type.UUID,                          null,   "Идентификатор существующего (в ГИС) элемента справочника");
        
        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Последний запрос на импорт в ГИС ЖКХ");
        col   ("uuid_message",              Type.UUID,                          null,   "UUID запроса в ГИС ЖКХ");
        col   ("elementguid_new",           Type.UUID,                          null,   "Идентификатор новой версии существующего (в ГИС) элемента справочника");
        
        key   (name, "uuid_object", "ts");
        key   ("uuid_out_soap", "uuid_out_soap");

        trigger ("BEFORE INSERT", "BEGIN "

            + "SELECT"

            + "      is_deleted, "
            + "      okei, " 
            + "      additionalservicetypename, " 
            + "      elementguid " 

            + "INTO " 

            + " :NEW.is_deleted, "
            + " :NEW.okei, " 
            + " :NEW.additionalservicetypename, " 
            + " :NEW.elementguid " 

            + " FROM tb_add_services WHERE uuid=:NEW.uuid_object; "

        + "END;");

        trigger ("AFTER INSERT", "BEGIN "
            + " RETURN;"
        + "END;");

    }

}