package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class ContractFileLog extends Table {

    public ContractFileLog () {
        
        super  ("tb_contract_files__log", "История изменения файлов, приложенных к договорам");
        
        pk     ("uuid",                  Type.UUID,                NEW_UUID,    "Ключ");
        col    ("action",                Type.STRING,                           "Действие");
        fk     ("uuid_object",           ContractFile.class,                    "Ссылка на запись");
        col    ("ts",                    Type.TIMESTAMP,           NOW,         "Дата/время события");
        fk     ("uuid_user",             VocUser.class,            null,        "Оператор");        
        col    ("ts_start_sending",      Type.DATE,                null,        "Дата начала передачи");
        
        fk     ("uuid_out_soap",         OutSoap.class,            null,        "Последний запрос на импорт в ГИС ЖКХ");
        col    ("uuid_message",          Type.UUID,                null,        "UUID запроса в ГИС ЖКХ");
        
        col    ("attachmentguid",        Type.UUID,                null,        "Идентификатор сохраненного вложения");
        col    ("attachmenthash",        Type.BINARY, 32,          null,        "ГОСТ Р 34.11-94");

        col    ("id_status",             Type.INTEGER, 1,          null,        "Статус");        
        col    ("label",                 Type.STRING,              null,        "Имя файла");
        col    ("mime",                  Type.STRING,              null,        "Тип содержимого");
        col    ("len",                   Type.INTEGER,             null,        "Размер, байт");
        col    ("body",                  Type.BLOB,                null,        "Содержимое");
        col    ("description",           Type.TEXT,                null,        "Примечание");
        col    ("purchasenumber",        Type.STRING, 60,          null,        "Номер извещения (для протокола открытого конкурса)");
        col    ("agreementnumber",       Type.STRING, 255,         null,        "Номер дополнительного соглашения");
        col    ("agreementdate",         Type.DATE,                null,        "Дата дополнительного соглашения");

        trigger ("BEFORE INSERT", "BEGIN "

            + "SELECT " +
                "id_status, " +
                "label, " +
                "mime," +
                "len, " +
                "body, " +
                "description," +
                "purchasenumber," +
                "agreementnumber," +
                "agreementdate " +
            " INTO " +               
                ":NEW.id_status, " +
                ":NEW.label, " +
                ":NEW.mime," +
                ":NEW.len, " +
                ":NEW.body, " +
                ":NEW.description," +
                ":NEW.purchasenumber," +
                ":NEW.agreementnumber," +
                ":NEW.agreementdate "
            + " FROM tb_contract_files WHERE uuid=:NEW.uuid_object; "

        + "END;");

    }    

}