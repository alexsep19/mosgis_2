package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocCharterObjectReason;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class CharterObjectLog extends Table {

    public CharterObjectLog () {
        
        super  ("tb_charter_objects__log", "История изменения объектов договоров управления");
        
        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        ref   ("action",                    VocAction.class,                            "Действие");
        fk    ("uuid_object",               CharterObject.class,                        "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,                 "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,         null,                "Оператор");
        fk    ("uuid_out_soap",             OutSoap.class,         null,                "Последний запрос на импорт в ГИС ЖКХ");
        
        col   ("is_deleted",                Type.BOOLEAN,          Bool.FALSE,          "1, если запись удалена; иначе 0");        
        col   ("startdate",                 Type.DATE,             null,                "Дата начала предоставления услуг");
        col   ("enddate",                   Type.DATE,             null,                "Дата окончания предоставления услуг");                
        
        col   ("annulmentinfo",             Type.STRING,           null,                "Причина аннулирования.");       
        col   ("is_annuled",                Type.BOOLEAN,          new Virt ("DECODE(\"ANNULMENTINFO\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");
        fk    ("id_reason",                 VocCharterObjectReason.class, null, "Основание");
        ref   ("uuid_charter_file",         CharterFile.class,    null,          "Ссылка на протокол");
        col   ("ismanagedbycontract",       Type.BOOLEAN,          null,   "Управление многоквартирным домом осуществляется управляющей организацией по договору управления");

        col   ("contractobjectversionguid", Type.UUID,             null,                "UUID этой версии данного объекта в ГИС ЖКХ");
        
        trigger ("BEFORE INSERT", "BEGIN "
                
           + "SELECT"
           + "       is_deleted,              "
           + "       annulmentinfo,           "
           + "       id_reason,               "
           + "       uuid_charter_file,       "                
           + "       ismanagedbycontract,     "
           + "       startdate,               "
           + "       enddate                  "
           + "INTO "                
           + "       :NEW.is_deleted,              "
           + "       :NEW.annulmentinfo,           "
           + "       :NEW.id_reason,               "
           + "       :NEW.uuid_charter_file,       "                
           + "       :NEW.ismanagedbycontract,     "
           + "       :NEW.startdate,               "
           + "       :NEW.enddate                  "
           + " FROM tb_charter_objects WHERE uuid=:NEW.uuid_object; "
                
        + "END;");

    }

}