package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class CharterObjectServiceLog extends Table {

    public CharterObjectServiceLog () {

        super  ("tb_charter_services__log", "История редактирования услуг по уставу");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        ref   ("action",                    VocAction.class,                            "Действие");
        fk    ("uuid_object",               CharterObjectService.class,     "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,                 "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,                      null,   "Оператор");
        
        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Последний запрос на импорт в ГИС ЖКХ");
        col   ("uuid_message",              Type.UUID,                          null,   "UUID запроса в ГИС ЖКХ");

        ref    ("uuid_charter_file",       CharterFile.class,       null,       "Ссылка на протокол");
        col    ("is_deleted",              Type.BOOLEAN,            null,       "1, если запись удалена; иначе 0");
        col    ("code_vc_nsi_3",           Type.STRING,  20,        null,       "Коммунальная услуга");
        ref    ("uuid_add_service",        AdditionalService.class, null,       "Дополнительная услуга");
        col    ("startdate",               Type.DATE,               null,       "Дата начала предоставления услуги");
        col    ("enddate",                 Type.DATE,               null,       "Дата окончания предоставления услуги");

       trigger ("BEFORE INSERT", "BEGIN "

           + "SELECT"
           + "       is_deleted,              "
           + "       code_vc_nsi_3,           "
           + "       uuid_add_service,        "
           + "       uuid_charter_file,       "
           + "       startdate,               "
           + "       enddate                  "
           + "INTO "                
           + "       :NEW.is_deleted,         "
           + "       :NEW.code_vc_nsi_3,           "
           + "       :NEW.uuid_add_service,        "
           + "       :NEW.uuid_charter_file,       "
           + "       :NEW.startdate,               "
           + "       :NEW.enddate                  "
           + " FROM "
           + "  tb_charter_services "
           + " WHERE uuid=:NEW.uuid_object; "

       + "END;");
       
    }

}