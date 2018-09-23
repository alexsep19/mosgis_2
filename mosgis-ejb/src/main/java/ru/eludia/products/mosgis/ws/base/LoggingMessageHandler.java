package ru.eludia.products.mosgis.ws.base;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class LoggingMessageHandler extends BaseLoggingMessageHandler implements SOAPHandler<SOAPMessageContext> {

/*    
    private void storeToDB (MessageInfo messageInfo, RequestHeader rh, String s) {

        try (DB db = ModelHolder.getModel ().getDb ()) {

            db.insert (InSoap.class, DB.HASH (                                    
                "svc",          messageInfo.getService (),
                "op",           messageInfo.getOperation (),
                "is_out",       messageInfo.isOut (),
                "uuid_sender",  rh.getSenderID (),
                "uuid_message", rh.getMessageGUID (),
                "dt",           rh.getDate (),
                "soap",         s
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
*/
    public boolean handleMessage (SOAPMessageContext messageContext) {

        SOAPMessage msg = messageContext.getMessage ();
        
        MessageInfo messageInfo = new MessageInfo (messageContext);
        
        byte [] bytes = toBytes (msg);
        
        String s = getLoggedMessge (messageInfo, bytes, getCharSetName (msg));
/*        
        if (!messageInfo.isOut) {

            RequestHeader rh = AbstactServiceAsync.getRequestHeader (msg);
            
            storeToDB (messageInfo, rh, s);

        }
*/        
        return true;
        
    }
        
}
