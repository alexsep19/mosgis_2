package ru.eludia.products.mosgis.db.model.ws;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.base.model.def.Num;
import ru.eludia.products.mosgis.db.model.tables.Sender;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

/**
 *
 * @author Aleksei
 */
public class WsMessages extends Table {

    public enum c implements ColEnum {
        UUID          (Type.UUID,                  NEW_UUID, "Ключ"),
        UUID_MESSAGE  (Type.UUID,                        "Идентификатор запроса, указанный поставщиком"),
        UUID_ORG      (VocOrganization.class,      null, "Организация"),
        REQUEST       (Type.TEXT,                  null, "Содержимое SOAP-запроса"),
        RESPONSE      (Type.TEXT,                  null, "XML ответа"),
        REQUEST_TIME  (Type.TIMESTAMP,             NOW,  "Дата/время записи запроса в БД"),
        RESPONSE_TIME (Type.TIMESTAMP,             null, "Дата/время генерации ответа БД"),
        ID_STATUS     (VocAsyncRequestState.class, new Num(VocAsyncRequestState.i.ACCEPTED.getId()), "Статус"),
        SERVICE       (Type.STRING,                 "Имя сервиса"),
        OPERATION     (Type.STRING,                 "Имя метода"),
        UUID_SENDER   (Sender.class,         null,  "Поставщик информации"),
        HAS_ERROR     (Type.BOOLEAN,         Bool.FALSE, "Наличие ошибки");
        
        @Override
        public Col getCol() {
            return col;
        }
        private Col col;

        private c(Type type, Object... p) {
            col = new Col(this, type, p);
        }

        private c(Class c, Object... p) {
            col = new Ref(this, c, p);
        }
    }

    public WsMessages() {
        super("ws_msgs", "Сообщения веб сервисов");

        cols(c.class);

        pk(c.UUID);
        key("uuid_org", c.UUID_ORG);
        key("uuid_message", c.UUID_MESSAGE);
    }
}
