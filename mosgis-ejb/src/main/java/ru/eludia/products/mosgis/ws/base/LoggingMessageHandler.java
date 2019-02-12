package ru.eludia.products.mosgis.ws.base;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.mos.gkh.gis.schema.integration.base.AckRequest;
import ru.mos.gkh.gis.schema.integration.base.HeaderType;
import ru.mos.gkh.gis.schema.integration.base.RequestHeader;
import ru.mos.gkh.gis.schema.integration.base.ResultHeader;

public class LoggingMessageHandler extends BaseLoggingMessageHandler {
   
    private static final String GET_STATE_OPERATION = "getState";
    
    private Object storeToDB(MessageInfo messageInfo, HeaderType rh, String s) {

        try (DB db = ModelHolder.getModel().getDb()) {
            String uuidOrg = null;
            if (RequestHeader.class.equals(rh.getClass())) {
                String orgPPAGuid = ((RequestHeader) rh).getOrgPPAGUID();
                uuidOrg = db.getString(db.getModel()
                        .select(VocOrganization.class, VocOrganization.c.UUID.lc())
                        .where(VocOrganization.c.ORGPPAGUID.lc(), orgPPAGuid));
            }

            String msgGuid = db.getString(db.getModel()
                    .select(WsMessages.class, WsMessages.c.UUID.lc())
                    .where(WsMessages.c.UUID_MESSAGE.lc(), rh.getMessageGUID()));
            if (StringUtils.isNotBlank(msgGuid))
                return msgGuid;
            return db.insertId(WsMessages.class, DB.HASH(
                    WsMessages.c.SERVICE,      messageInfo.getService(),
                    WsMessages.c.OPERATION,    messageInfo.getOperation(),
                    WsMessages.c.UUID_ORG,     uuidOrg,
                    WsMessages.c.UUID_MESSAGE, rh.getMessageGUID(),
                    WsMessages.c.REQUEST,      s
            ));

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Cannot log SOAP message", ex);

            throw new IllegalStateException("Cannot log SOAP message", ex);
        }

    }
    
    @Override
    public boolean handleMessage (SOAPMessageContext messageContext) {

        SOAPMessage msg = messageContext.getMessage ();
        
        MessageInfo messageInfo = new MessageInfo (messageContext);
        
        if (!messageInfo.isOut && !GET_STATE_OPERATION.equals(messageInfo.operation)) {
            byte [] bytes = toBytes (msg);
            String s = getLoggedMessge (messageInfo, bytes, getCharSetName (msg));
            
            HeaderType rh = AbstactServiceAsync.getHeader(msg, HeaderType.class);

            Object msgId = storeToDB (messageInfo, rh, s);

            AckRequest.Ack ack = new AckRequest.Ack();
            ack.setMessageGUID(msgId.toString());
            ack.setRequesterMessageGUID(rh.getMessageGUID());
            addParamToMessageContext(messageContext, "ack", ack);
            addParamToMessageContext(messageContext, "msgId", MessageContext.Scope.APPLICATION);
        } else if (messageInfo.isOut && GET_STATE_OPERATION.equals(messageInfo.operation)){
            ResultHeader resultHeader = new ResultHeader();
            resultHeader.setDate(LocalDateTime.now());
            resultHeader.setMessageGUID(messageContext.get("msgId").toString());
            AbstactServiceAsync.addHeaderToResponse(msg, resultHeader);
        }
                
        return true;
        
    }
    
    private void addParamToMessageContext(SOAPMessageContext messageContext, String name, Object value) {
        messageContext.put(name, value);
        messageContext.setScope(name, MessageContext.Scope.APPLICATION);
    }
}
