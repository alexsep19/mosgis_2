package ru.eludia.products.mosgis.db.model.ws;

import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Num;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.Sender;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.util.StringUtils;

/**
 *
 * @author Aleksei
 */
public class WsMessages extends Table {

	private static final QName SOAP_SERVER_FAULT = new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Server",
			SOAPConstants.SOAP_ENV_PREFIX);

	private static Logger logger = Logger.getLogger(OutSoap.class.getName());

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

        private c(Class<?> c, Object... p) {
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
    
	public static void registerException(DB db, Object uuid, Exception ex) throws SQLException {
		try {
			SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
			SOAPBody soapBody = soapMessage.getSOAPBody();
			soapBody.addFault(SOAP_SERVER_FAULT, 
					ex.getClass().getName() + (StringUtils.isNotBlank(ex.getMessage()) ? ": " + ex.getMessage() : ""));

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			soapMessage.writeTo(out);
			String responseStr = new String(out.toByteArray(), "UTF-8");

			db.update(WsMessages.class, DB.HASH(
					WsMessages.c.UUID,          uuid, 
					WsMessages.c.RESPONSE_TIME, LocalDateTime.now(), 
					WsMessages.c.RESPONSE,      responseStr,
					WsMessages.c.HAS_ERROR,     true, 
					WsMessages.c.ID_STATUS,     VocAsyncRequestState.i.DONE.getId()));
		} catch (SOAPException | IOException e) {
			logger.log(Level.SEVERE, "Cannot create SOAP fault", e);
		}
	}
}
