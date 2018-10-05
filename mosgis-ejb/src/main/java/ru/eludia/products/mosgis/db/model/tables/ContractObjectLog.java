package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class ContractObjectLog extends Table {

    public ContractObjectLog () {
        
        super  ("tb_contract_objects__log", "История изменения объектов договоров управления");
        
        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        ref   ("action",                    VocAction.class,                            "Действие");
        fk    ("uuid_object",               ContractObject.class,                       "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,                 "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,         null,                "Оператор");
        fk    ("uuid_out_soap",             OutSoap.class,         null,                "Последний запрос на импорт в ГИС ЖКХ");
        
        col   ("is_deleted",                Type.BOOLEAN,          Bool.FALSE,          "1, если запись удалена; иначе 0");        
        ref   ("uuid_contract_agreement",   ContractFile.class,    null,                "Ссылка на дополнительное соглашение");        
        col   ("startdate",                 Type.DATE,                                  "Дата начала предоставления услуг");
        col   ("enddate",                   Type.DATE,                                  "Дата окончания предоставления услуг");                

        col   ("contractobjectversionguid", Type.UUID,             null,                "UUID этой версии данного объекта в ГИС ЖКХ");
        
        trigger ("BEFORE INSERT", "BEGIN "
                
           + "SELECT"
           + "       is_deleted,              "
           + "       uuid_contract_agreement, "
           + "       startdate,               "
           + "       enddate                  "
           + "INTO "                
           + "       :NEW.is_deleted,              "
           + "       :NEW.uuid_contract_agreement, "
           + "       :NEW.startdate,               "
           + "       :NEW.enddate                  "
           + " FROM tb_contract_objects WHERE uuid=:NEW.uuid_object; "
                
        + "END;");

    }

}