package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import static ru.eludia.base.model.def.Bool.FALSE;
import static ru.eludia.base.model.def.Def.*;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.IN_PROGRESS;


public class OutSoap extends Table {

    public OutSoap () {

        super ("out_soap", "Все исходящие запросы, в исходной форме");

        pk  ("uuid",         Type.UUID,               "Отправляемый MessageGUID");
        col ("uuid_ack",     Type.UUID,         null, "UUID на стороне ГИС");

        col ("is_out",       Type.BOOLEAN,            "1 для исходящих, 0 для входящих");
        col ("svc",          Type.STRING,             "Имя сервиса");
        col ("op",           Type.STRING,             "Имя метода");
        col ("rq",           Type.TEXT,         null, "Содержимое SOAP-запроса");
        col ("rp",           Type.TEXT,         null, "XML ответа");
        col ("ts",           Type.TIMESTAMP,    NOW,  "Дата/время записи запроса в БД");
        col ("ts_rp",        Type.TIMESTAMP,    null, "Дата/время записи ответа в БД");
        col ("is_failed",    Type.BOOLEAN,     FALSE, "1 для аварийных, 0 для остальных");
        col ("err_code",     Type.STRING,       null, "Код ошибки");
        col ("err_text",     Type.STRING,       null, "Текст ошибки");
        col ("ym",           Type.STRING,       new Virt ("TO_CHAR(TS,'yyyy-mm')"), "Год-Месяц");

        fk ("id_status",     VocAsyncRequestState.class, new Num (IN_PROGRESS.getId ()), "Статус");

        unique ("uuid_ack", "uuid_ack");
        
        key ("ym", "ym", "ts");
        
        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "
            + "IF :NEW.rp IS NULL THEN :NEW.ts_rp := NULL; ELSE :NEW.ts_rp := SYSDATE; END IF;"
        + "END;");

    }

}