package ru.eludia.products.mosgis.ws.soap.tools;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.logging.Logger;
import java.util.UUID;
import java.util.logging.Level;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.gosuslugi.dom.schema.integration.base.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.base.HeaderType;
import ru.gosuslugi.dom.schema.integration.base.ResultHeader;

public abstract class LoggingOutMessageHandler extends BaseLoggingMessageHandler implements SOAPHandler<SOAPMessageContext> {

    static final ObjectFactory of = new ObjectFactory ();
    public static final String FIELD_MESSAGE_GUID = "MessageGUID";
    public static final String FIELD_ORG_PPA_GUID = "OrgPPAGuid";
    
    final UUID getMessageGUID (SOAPMessageContext messageContext) {
        UUID uuid = (UUID) messageContext.get (FIELD_MESSAGE_GUID);
        return uuid == null ? UUID.randomUUID () : uuid;
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
    
    String getOrgPPAGUID (HeaderType rh) {
        return null;
    }
            
    private void store (MessageInfo messageInfo, HeaderType rh, String s) {

        try (DB db = ModelHolder.getModel ().getDb ()) {

            db.insert (OutSoap.class, DB.HASH (     
                "svc",          messageInfo.getService (),
                "op",           messageInfo.getOperation (),
                "is_out",       messageInfo.isOut (),
                "uuid",         rh.getMessageGUID (),
                "orgppaguid",   getOrgPPAGUID (rh),
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
    
    abstract HeaderType createRequestHeader (SOAPMessageContext messageContext);
    
    @Override
    public final boolean handleMessage (SOAPMessageContext messageContext) {

        SOAPMessage msg = messageContext.getMessage ();
        
        MessageInfo messageInfo = new MessageInfo (messageContext);        
        
        final boolean isGetState = "getState".equals (messageInfo.operation);
            
        if (messageInfo.isOut) {
            
            HeaderType rh = createRequestHeader (messageContext);
            rh.setDate (DB.to.XMLGregorianCalendar (new java.sql.Timestamp (System.currentTimeMillis ())));
            rh.setMessageGUID (getMessageGUID (messageContext).toString ());
            
            try {
                SOAPTools.setRequestHeader (msg, rh);
            }
            catch (Exception ex) {
                logger.log (Level.SEVERE, null, ex);
            }

            final String xml = getLoggedMessge (messageInfo, toBytes (msg), getCharSetName (msg)); // MUST be called AFTER setRequestHeader; side effect: logs out the message

            if (!isGetState) store (messageInfo, rh, xml);

        }
        else {

            final String xml = getLoggedMessge (messageInfo, toBytes (msg), getCharSetName (msg)); // side effect: logs out the message

            if (isGetState) update (SOAPTools.getResultHeader (msg), xml);            

        }

        return true;

    }

}