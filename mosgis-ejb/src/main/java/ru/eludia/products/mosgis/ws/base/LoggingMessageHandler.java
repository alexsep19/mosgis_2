package ru.eludia.products.mosgis.ws.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import ru.eludia.base.DB;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.model.tables.Sender;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.eludia.products.mosgis.web.base.Errors;
import ru.eludia.products.mosgis.web.base.Fault;
import ru.mos.gkh.gis.schema.integration.base.AckRequest;
import ru.mos.gkh.gis.schema.integration.base.HeaderType;
import ru.mos.gkh.gis.schema.integration.base.RequestHeader;
import ru.mos.gkh.gis.schema.integration.base.ResultHeader;

public class LoggingMessageHandler extends BaseLoggingMessageHandler {
   
    private static final String GET_STATE_OPERATION = "getState";
    
    private Map<String, Object> storeToDB(Map<String, Object> sender, MessageInfo messageInfo, HeaderType rh, String s) {

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
                    .where(WsMessages.c.UUID_MESSAGE, rh.getMessageGUID())
                    .and(WsMessages.c.UUID_SENDER, sender.get("uuid")) );
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
                    WsMessages.c.REQUEST,      s,
                    WsMessages.c.UUID_SENDER,  sender.get("uuid")
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
        
        Map<String, Object> sender = null;
        if (!messageInfo.isOut) {
            try {
                HttpServletResponse response =  (HttpServletResponse) messageContext.get(MessageContext.SERVLET_RESPONSE);
                
                sender = getSender(messageContext);
                if (sender == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return false;
                } else if (TypeConverter.Boolean(sender.get(Sender.c.IS_LOCKED.lc()))) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
                addParamToMessageContext(messageContext, "sender", sender);
            } catch (IOException ex) {
                throw new RuntimeException("Cannot load IS", ex);
            }
        }
        
        if (!messageInfo.isOut && !GET_STATE_OPERATION.equals(messageInfo.operation)) {
            byte [] bytes = toBytes (msg);
            String s = getLoggedMessge (messageInfo, bytes, getCharSetName (msg));
            
            HeaderType rh = AbstactServiceAsync.getHeader(msg, HeaderType.class);

            Map<String, Object> savedMsg = storeToDB (sender, messageInfo, rh, s);

            AckRequest.Ack ack = new AckRequest.Ack();
            ack.setMessageGUID(savedMsg.get("msgId").toString());
            ack.setRequesterMessageGUID(rh.getMessageGUID());
            addParamToMessageContext(messageContext, "ack", ack);
            addParamToMessageContext(messageContext, "msgId", rh.getMessageGUID());
            addParamToMessageContext(messageContext, "isNew", savedMsg.get("isNew"));
        } else if (messageInfo.isOut && messageContext.get("response") != null) { 
            try {
                InputStream is = new ByteArrayInputStream(messageContext.get("response").toString().getBytes("UTF-8"));
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
    
    private Map<String, Object> getSender(SOAPMessageContext messageContext) throws IOException {        
        Map headers = (Map) messageContext.get(MessageContext.HTTP_REQUEST_HEADERS);
        ArrayList authBlock = (ArrayList) headers.get("Authorization");
        if (authBlock == null || authBlock.isEmpty()) {
            return null;
        }
        
        String userpass = ((String) authBlock.get(0)).substring(6);
        byte[] buf = Base64.getDecoder().decode(userpass.getBytes());
        String credentials = new String(buf);
        
        String username;
        String password;
        int p = credentials.indexOf(":");
        if (p > -1) {
            username = credentials.substring(0, p);
            password = credentials.substring(p + 1);
        } else {
            return null;
        }
        try (DB db = ModelHolder.getModel().getDb()) {
            Map<String, Object> sender = db.getMap(db.getModel()
                    .select(Sender.class, "*")
                    .where(Sender.c.LOGIN.lc(), username)
                    .and("is_deleted", 0));
            if (sender == null || sender.isEmpty())
                return null;
            if(!sender.get(Sender.c.SHA1.lc()).toString().equals(VocUser.encrypt((UUID)sender.get(Sender.c.SALT.lc()), password)))
                return null;
            return sender;
        } catch (SQLException ex) {
            return null;
        }
        
    }
}
