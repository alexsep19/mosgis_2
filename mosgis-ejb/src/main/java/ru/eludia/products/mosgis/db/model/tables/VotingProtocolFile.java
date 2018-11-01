package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;

public class VotingProtocolFile extends Table {
    
    public VotingProtocolFile () {
        
        super ("tb_voting_protocol_files", "Файлы, приложенные к протоколу ОСС");
        
        pk  ("uuid", Type.UUID, "Ключ");
        
        ref ("uuid_voting_protocol", VotingProtocol.class, "Ссылка на протокол общего собрания собственников");
        
        col ("description", Type.STRING, 500, "Описание вложения");
        
        col ("label",                 Type.STRING,                           "Имя файла");
        col ("mime",                  Type.STRING,              null,        "Тип содержимого");
        col ("len",                   Type.INTEGER,             null,        "Размер, байт");
        col ("body",                  Type.BLOB,                EMPTY_BLOB,  "Содержимое");
        col ("attachmentguid",        Type.UUID,                null,        "Идентификатор сохраненного вложения");
        col ("attachmenthash",        Type.BINARY, 32,          null,        "ГОСТ Р 34.11-94");
        
        fk  ("id_log",                VotingProtocolFileLog.class,    null,  "Последнее событие редактирования");
        
    }
    
}
