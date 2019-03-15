package ru.eludia.products.mosgis.ws.soap.impl.base;

import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jms.Queue;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.ws.soap.tools.LoggingInMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;

public abstract class BaseServiceAsync {
    
    protected Logger logger = Logger.getLogger (getClass ().getName ());

    @Resource
    protected WebServiceContext wsContext;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;

    protected AckRequest publishIfNew (Queue queue) {
        
        MessageContext msgContext = wsContext.getMessageContext ();
        
        AckRequest result = LoggingInMessageHandler.getAckRequest (msgContext);
        
        if (queue != null && LoggingInMessageHandler.isNewRequest (msgContext))
            UUIDPublisher.publish (queue, result.getAck ().getMessageGUID ());
        
        return result;
        
    }

}