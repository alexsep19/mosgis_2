package ru.eludia.products.mosgis.ws.soap.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.ws.rs.NotFoundException;
import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import static ru.eludia.base.db.util.TypeConverter.hex;

public abstract class BaseLoggingMessageHandler implements SOAPHandler<SOAPMessageContext> {
        
    protected static final String defaultCharSetName = "UTF-8";
    
    protected final Logger logger = Logger.getLogger (getClass ().getName ());
    
    protected static final String getCharSetName (SOAPMessage msg) {
        
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
    
    protected final class MessageInfo {
        
        Boolean isOut;
        String service;
        String operation;
                
        MessageInfo (SOAPMessageContext messageContext) {            
            this.isOut     = (Boolean) messageContext.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            this.service   = ((javax.xml.namespace.QName) messageContext.get (MessageContext.WSDL_SERVICE)).getLocalPart ();
            try {
                this.operation = ((javax.xml.namespace.QName) messageContext.get (MessageContext.WSDL_OPERATION)).getLocalPart ();
            } catch (Exception e) {
                throw new NotFoundException("Операция не найдена в сервисе " + service);
            }
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
    
    protected static final byte [] toBytes (SOAPMessage msg) {
                
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();                
            
        try {
            msg.writeTo (baos);
        }
        catch (SOAPException | IOException ex) {
            throw new IllegalStateException (ex);
        }
            
        return baos.toByteArray ();
        
    }    
    
    protected final String getLoggedMessge (MessageInfo messageInfo, byte [] bytes, String charSetName) {
        
        try {
            
            String s = new String (bytes, charSetName);

            logger.log (Level.INFO, () -> {
                
                StringBuilder sb = new StringBuilder (messageInfo.toString ());
                
                char last = '0';
                
                for (int i = 0; i < s.length (); i ++) {
                    
                    char c = s.charAt (i);
                    
                    switch (c) {
                        case '\n':
                        case '\r':
                            c = ' ';
                    }
                    
                    if (c == ' ' && last == ' ') continue;
                    
                    sb.append (last = c);
                    
                }
                
                return sb.toString ();
                
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
    
    @Override
    public final Set<QName> getHeaders () {
        return Collections.EMPTY_SET;
    }
    
    @Override
    public final boolean handleFault (SOAPMessageContext messageContext) {
        return true;
    }
    
    @Override
    public final void close (MessageContext context) {
    }
    
}
