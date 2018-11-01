package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class VotingProtocolFileLog extends Table {
    
    public VotingProtocolFileLog () {
        
        super ("tb_voting_protocol_files__log", "История изменения файлов, приложенных к протоколам ОСС");
        
        pk     ("uuid",                  Type.UUID,                NEW_UUID,    "Ключ");
        ref    ("action",                VocAction.class,                       "Действие");
        fk     ("uuid_object",           VotingProtocolFile.class,              "Ссылка на запись");
        col    ("ts",                    Type.TIMESTAMP,           NOW,         "Дата/время события");
        fk     ("uuid_user",             VocUser.class,            null,        "Оператор");        
        col    ("ts_start_sending",      Type.DATE,                null,        "Дата начала передачи");
        
        fk     ("uuid_out_soap",         OutSoap.class,            null,        "Последний запрос на импорт в ГИС ЖКХ");
        col    ("uuid_message",          Type.UUID,                null,        "UUID запроса в ГИС ЖКХ");
        
        col    ("id_status",             Type.INTEGER, 1,          null,        "Статус");        
        col    ("label",                 Type.STRING,              null,        "Имя файла");
        col    ("mime",                  Type.STRING,              null,        "Тип содержимого");
        col    ("len",                   Type.INTEGER,             null,        "Размер, байт");
        col    ("body",                  Type.BLOB,                null,        "Содержимое");
        col    ("description",           Type.TEXT,                null,        "Примечание");
        
        col    ("ts_error",              Type.TIMESTAMP,           null,        "Дата/время ошибки передачи");
        col    ("err_text",              Type.STRING,              null,        "Текст ошибки");
        
        trigger ("BEFORE INSERT", 
                "BEGIN "
                        + "SELECT " +
                            "id_status, " +
                            "label, " +
                            "mime," +
                            "len, " +
                            "body, " +
                            "description" +
                        " INTO " +               
                            ":NEW.id_status, " +
                            ":NEW.label, " +
                            ":NEW.mime," +
                            ":NEW.len, " +
                            ":NEW.body, " +
                            ":NEW.description" +
                        " FROM tb_voting_protocol_files WHERE uuid=:NEW.uuid_object; "

                + "END;");
        
    }
}
