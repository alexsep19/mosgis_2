package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import static ru.eludia.base.model.def.Bool.TRUE;
import static ru.eludia.base.model.def.Bool.FALSE;
import static ru.eludia.base.model.def.Def.*;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.IN_PROGRESS;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.Fault;


public class OutSoap extends Table {
    
    private static Logger logger = Logger.getLogger (OutSoap.class.getName ());

    public OutSoap () {

        super ("out_soap", "Все исходящие запросы, в исходной форме");

        pk  ("uuid",         Type.UUID,               "Отправляемый MessageGUID");
        col ("uuid_ack",     Type.UUID,         null, "UUID на стороне ГИС");

        col ("is_out",       Type.BOOLEAN,      TRUE, "1 для исходящих, 0 для входящих");
        col ("svc",          Type.STRING,             "Имя сервиса");
        col ("op",           Type.STRING,             "Имя метода");
        col ("rq",           Type.TEXT,         null, "Содержимое SOAP-запроса");
        col ("rp",           Type.TEXT,         null, "XML ответа");
        col ("ts",           Type.TIMESTAMP,    NOW,  "Дата/время записи запроса в БД");
        col ("ts_rp",        Type.TIMESTAMP,    null, "Дата/время записи ответа в БД");
        col ("is_failed",    Type.BOOLEAN,     FALSE, "1 для аварийных, 0 для остальных");
        col ("err_code",     Type.STRING,       null, "Код ошибки");
        col ("err_text",     Type.STRING,       null, "Текст ошибки");
        col ("orgppaguid",     Type.UUID,       null, "Идентификатор зарегистрированной организации, с которым отправлялся запрос");
        col ("ym",           Type.STRING,       new Virt ("TO_CHAR(TS,'yyyy-mm')"), "Год-Месяц");

        fk ("id_status",     VocAsyncRequestState.class, new Num (IN_PROGRESS.getId ()), "Статус");

        unique ("uuid_ack", "uuid_ack");
        
        key ("ym", "ym", "ts");
                
        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "
            + "IF :NEW.rp IS NULL THEN :NEW.ts_rp := NULL; ELSE :NEW.ts_rp := SYSDATE; END IF;"
        + "END;");

    }
    
    public static final void expire (DB db, UUID uuid) throws SQLException {
        
        db.update (OutSoap.class, DB.HASH (
            "uuid", uuid,
            "id_status", 3,
            "is_failed", 1,
            "err_code", "0",
            "ts_rp",    NOW,
            "err_text", "Операция прервана по истечении времени"
        ));
        
    }
    
    public static final UUID addExpired (DB db) throws SQLException {
        
        UUID uuid = UUID.randomUUID ();        
        
        db.insert (OutSoap.class, DB.HASH (
                "uuid", uuid,
                "svc",  "null",
                "op",   "null",
                "rq",   "<null />",
                "rp",   "<null />",
                "ts",   NOW,
                "ts_rp",   NOW,
                "id_status", 3,
                "is_failed", 1,
                "err_code", "0",
                "err_text", "Операция прервана по истечении времени"
        ));
        
        return uuid;
        
    }
    
    public final static void registerAck (DB db, AckRequest.Ack ack) throws SQLException {

        db.update (OutSoap.class, DB.HASH (
            "uuid",     ack.getRequesterMessageGUID (),
            "uuid_ack", ack.getMessageGUID ()
        ));

    }
    
    public static void registerException (DB db, Object uuid, String svc, String op, Exception ex) throws SQLException {
        
        db.upsert (OutSoap.class, DB.HASH ("uuid", uuid,
            "svc", svc,
            "op",   op,
            "is_out",  1,
            "id_status", DONE.getId (),
            "is_failed", 1,
            "err_code",  "0",
            "err_text",  ex.getMessage ()
        ));
        
    }
    
    public static void registerFault (DB db, Object uuid, Fault faultInfo) throws SQLException {

        db.update (OutSoap.class, DB.HASH (
            "uuid", uuid,
            "id_status", DONE.getId (),
            "is_failed", 1,
            "err_code",  faultInfo.getErrorCode (),
            "err_text",  faultInfo.getErrorMessage ()
        ));

    }

}