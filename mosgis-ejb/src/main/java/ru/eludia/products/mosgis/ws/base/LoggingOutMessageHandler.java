package ru.eludia.products.mosgis.ws.base;

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
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jmx.Conf;
import ru.gosuslugi.dom.schema.integration.base.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.base.RequestHeader;
import ru.gosuslugi.dom.schema.integration.base.ResultHeader;

public class LoggingOutMessageHandler extends BaseLoggingMessageHandler implements SOAPHandler<SOAPMessageContext> {

    private static final String defaultCharSetName = "UTF-8";
    private static final ObjectFactory of = new ObjectFactory ();
    public static final String FIELD_MESSAGE_GUID = "MessageGUID";
    public static final String FIELD_ORG_PPA_GUID = "OrgPPAGuid";
    
    private UUID getMessageGUID (SOAPMessageContext messageContext) {
        UUID uuid = (UUID) messageContext.get (FIELD_MESSAGE_GUID);
        return uuid == null ? UUID.randomUUID () : uuid;
    }

    private UUID getOrgPPAGuid (SOAPMessageContext messageContext) {
        UUID uuid = (UUID) messageContext.get (FIELD_ORG_PPA_GUID);
        return uuid == null ? UUID.fromString (Conf.get (VocSetting.i.GIS_ID_ORGANIZATION)) : uuid;
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
            rh.setMessageGUID (getMessageGUID (messageContext).toString ());
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
        
}
