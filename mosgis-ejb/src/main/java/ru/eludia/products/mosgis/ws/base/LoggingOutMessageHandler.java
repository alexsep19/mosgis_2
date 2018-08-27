package ru.eludia.products.mosgis.ws.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import ru.eludia.base.DB;
import static ru.eludia.base.db.util.TypeConverter.hex;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.gosuslugi.dom.schema.integration.base.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.base.RequestHeader;
import ru.gosuslugi.dom.schema.integration.base.ResultHeader;

public class LoggingOutMessageHandler implements SOAPHandler<SOAPMessageContext> {

    private static final String defaultCharSetName = "UTF-8";
    private static final ObjectFactory of = new ObjectFactory ();
    public static final String FIELD_ORG_PPA_GUID = "OrgPPAGuid";

    private final static Logger logger = Logger.getLogger (LoggingOutMessageHandler.class.getName ());

    private UUID getOrgPPAGuid (SOAPMessageContext messageContext) {
        UUID uuid = (UUID) messageContext.get (FIELD_ORG_PPA_GUID);
logger.info ("getOrgPPAGuid: " + uuid);
        return uuid == null ? UUID.fromString (VocSetting.i.GIS_ID_ORGANIZATION.getValue ()) : uuid;
    }

    private static final String getCharSetName (SOAPMessage msg) {
        
        MimeHeaders mimeHeaders = msg.getMimeHeaders ();

        if (mimeHeaders == null) return defaultCharSetName;
        
        String [] contentTypes = mimeHeaders.getHeader ("Content-Type");
        
        if (contentTypes == null || contentTypes.length == 0) return defaultCharSetName;
        
        if (contentTypes.length > 1) throw new IllegalArgumentException (contentTypes.length + " Content-Type headers are not allowed");
                
        StringTokenizer st = new StringTokenizer (contentTypes [0], "; ");
        
        final String prefix = "charset=";

        while (st.hasMoreTokens ()) {

            String token = st.nextToken ();

            if (token.startsWith (prefix)) return token.substring (prefix.length ());

        }

        return defaultCharSetName;
        
    }

    private void update (ResultHeader rh, String s) {
        
        try (DB db = ModelHolder.getModel ().getDb ()) {

            db.update (OutSoap.class, DB.HASH (     
                "uuid",         rh.getMessageGUID (),
                "rp",           s
            ));

        }
        catch (SQLException ex) {
            Logger.getLogger (LoggingOutMessageHandler.class.getName()).log (Level.SEVERE, null, ex);
        }    
    
    }
    
    private class MessageInfo {
        
        Boolean isOut;
        String service;
        String operation;
                
        MessageInfo (SOAPMessageContext messageContext) {            
            this.isOut     = (Boolean) messageContext.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);            
            this.service   = ((javax.xml.namespace.QName) messageContext.get (MessageContext.WSDL_SERVICE)).getLocalPart ();
            this.operation = ((javax.xml.namespace.QName) messageContext.get (MessageContext.WSDL_OPERATION)).getLocalPart ();            
        }
        
        @Override
        public final String toString () {            
            StringBuilder sb = new StringBuilder (service);
            sb.append ('.');
            sb.append (operation);
            sb.append (isOut ? " -} " : " {- ");
            return sb.toString ();
        }

        public Boolean isOut () {
            return isOut;
        }

        public String getService () {
            return service;
        }

        public String getOperation () {
            return operation;
        }
        
    }
    
    private static byte [] toBytes (SOAPMessage msg) {
                
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();                
            
        try {
            msg.writeTo (baos);
        }
        catch (SOAPException | IOException ex) {
            throw new IllegalStateException (ex);
        }
            
        return baos.toByteArray ();
        
    }    
    
    private String getLoggedMessge (MessageInfo messageInfo, byte [] bytes, String charSetName) {
        
        try {
            
            String s = new String (bytes, charSetName);
            
            logger.log (Level.INFO, "{0}{1}", new Object [] {
                messageInfo.toString (), 
                s
                    .replace ("\r", "\\r")
                    .replace ("\n", "\\n")
            });
            
            return s;
            
        }
        catch (UnsupportedEncodingException ex) {

            logger.log (Level.SEVERE, "{0} BROKEN {1} ENCODING {2}", new Object [] {
                messageInfo.toString (), 
                charSetName,
                hex (bytes)
            });
            
            throw new IllegalArgumentException ("HTTP body does not fit to declared character set: " + charSetName);

        }
        
    }
    
    private void store (MessageInfo messageInfo, RequestHeader rh, String s) {

        try (DB db = ModelHolder.getModel ().getDb ()) {

            db.insert (OutSoap.class, DB.HASH (     
                "svc",          messageInfo.getService (),
                "op",           messageInfo.getOperation (),
                "is_out",       messageInfo.isOut (),
                "uuid",         rh.getMessageGUID (),
                "ts",           rh.getDate (),
                "rq",           s
            ));

        }
        catch (SQLException ex) {

            if (ex instanceof SQLIntegrityConstraintViolationException && ex.getErrorCode () == 1) {
                
                throw new IllegalStateException ("MessageGUID=" + rh.getMessageGUID () + " was already registered");
                
            }
            
            logger.log (Level.SEVERE, "Cannot log SOAP message", ex);

            throw new IllegalStateException ("Cannot log SOAP message", ex);
            
        }
        
    }    
    
    public boolean handleMessage (SOAPMessageContext messageContext) {

        SOAPMessage msg = messageContext.getMessage ();
        
        MessageInfo messageInfo = new MessageInfo (messageContext);                        
        
        if (messageInfo.isOut) {
            
            RequestHeader rh = of.createRequestHeader ();

            rh.setDate (DB.to.XMLGregorianCalendar (new java.sql.Timestamp (System.currentTimeMillis ())));
            rh.setMessageGUID (UUID.randomUUID ().toString ());            
            rh.setOrgPPAGUID (getOrgPPAGuid (messageContext).toString ());
            rh.setIsOperatorSignature (Boolean.TRUE);
            
            try {

                AbstactServiceAsync.setRequestHeader (msg, rh);
                                
            }
            catch (Exception ex) {
                Logger.getLogger (LoggingOutMessageHandler.class.getName()).log (Level.SEVERE, null, ex);
            }
            
            byte [] bytes = toBytes (msg);
        
            String s = getLoggedMessge (messageInfo, bytes, getCharSetName (msg));

            if (!"getState".equals (messageInfo.operation)) store (messageInfo, rh, s);

        }
        else {
            
            if ("getState".equals (messageInfo.operation)) {
                
                byte [] bytes = toBytes (msg);

                String s = getLoggedMessge (messageInfo, bytes, getCharSetName (msg));

                ResultHeader resultHeader = AbstactServiceAsync.getResultHeader (msg);
                
                update (resultHeader, s);
            
            }            
            
        }

        return true;
        
    }
    
    public Set<QName> getHeaders () {
        return Collections.EMPTY_SET;
    }
    
    public boolean handleFault (SOAPMessageContext messageContext) {
        return true;
    }
    
    public void close (MessageContext context) {
    }
    
}
