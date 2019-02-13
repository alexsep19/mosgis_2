package ru.eludia.products.mosgis.ws.base;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.mos.gkh.gis.schema.integration.base.AckRequest;
import ru.mos.gkh.gis.schema.integration.base.HeaderType;
import ru.mos.gkh.gis.schema.integration.base.RequestHeader;
import ru.mos.gkh.gis.schema.integration.base.ResultHeader;

public class LoggingMessageHandler extends BaseLoggingMessageHandler {
   
    private static final String GET_STATE_OPERATION = "getState";
    
    private Map<String, Object> storeToDB(MessageInfo messageInfo, HeaderType rh, String s) {

        Map<String, Object> result = new HashMap<>();
        
        try (DB db = ModelHolder.getModel().getDb()) {
            String uuidOrg = null;
            if (RequestHeader.class.equals(rh.getClass())) {
                String orgPPAGuid = ((RequestHeader) rh).getOrgPPAGUID();
                uuidOrg = db.getString(db.getModel()
                        .select(VocOrganization.class, VocOrganization.c.UUID.lc())
                        .where(VocOrganization.c.ORGPPAGUID.lc(), orgPPAGuid));
            }

            Map<String, Object> msg = db.getMap(db.getModel()
                    .select(WsMessages.class, WsMessages.c.UUID.lc())
                    .where(WsMessages.c.UUID_MESSAGE.lc(), rh.getMessageGUID()));
            if (msg != null && !msg.isEmpty()) {
                result.put("isNew", false);
                result.put("msgId", msg.get(WsMessages.c.UUID.lc()));
                return result;
            }
            result.put("isNew", true);
            result.put("msgId", db.insertId(WsMessages.class, DB.HASH(
                    WsMessages.c.SERVICE,      messageInfo.getService(),
                    WsMessages.c.OPERATION,    messageInfo.getOperation(),
                    WsMessages.c.UUID_ORG,     uuidOrg,
                    WsMessages.c.UUID_MESSAGE, rh.getMessageGUID(),
                    WsMessages.c.REQUEST,      s
            )));
            return result;
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

            Map<String, Object> savedMsg = storeToDB (messageInfo, rh, s);

            AckRequest.Ack ack = new AckRequest.Ack();
            ack.setMessageGUID(savedMsg.get("msgId").toString());
            ack.setRequesterMessageGUID(rh.getMessageGUID());
            addParamToMessageContext(messageContext, "ack", ack);
            addParamToMessageContext(messageContext, "msgId", savedMsg.get("msgId"));
            addParamToMessageContext(messageContext, "isNew", savedMsg.get("isNew"));
        } else if (messageInfo.isOut && messageContext.get("response") != null) { 
            InputStream is = new ByteArrayInputStream(messageContext.get("response").toString().getBytes());
            try {
                SOAPMessage response = MessageFactory.newInstance().createMessage(null, is);
                messageContext.setMessage(response);
            } catch (Exception ex) {
                throw new IllegalStateException("Cannot create SOAP message", ex);
            }
        } else if (messageInfo.isOut){
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
