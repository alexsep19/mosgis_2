package ru.eludia.products.mosgis.db.model.incoming.soap;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import static ru.eludia.base.model.def.Def.*;
import ru.eludia.base.model.def.Num;
import ru.eludia.products.mosgis.db.model.tables.Sender;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;

public class InSoap extends Table {

    public InSoap () {
        
        super ("in_soap", "Все входящие запросы, в исходной форме");
        
        pk  ("uuid_message", Type.UUID,               "Ключ");
        
        col ("is_out",       Type.BOOLEAN,            "1 для исходящих, 0 для входящих");
        col ("svc",          Type.STRING,             "Имя сервиса");
        col ("op",           Type.STRING,             "Имя метода");
        col ("soap",         Type.TEXT,               "Содержимое SOAP-запроса");
        col ("response",     Type.TEXT,         null, "XML ответа");
        col ("dt",           Type.DATETIME,           "Дата/время из исходного запроса");
        col ("ts",           Type.TIMESTAMP, NOW,     "Дата/время записи в БД");
        
        fk ("id_status",    VocAsyncRequestState.class, Num.ONE, "Статус");
        fk ("uuid_sender",  Sender.class,                        "Ссылка на источник данных");

    }
    
}