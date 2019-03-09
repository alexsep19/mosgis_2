package ru.eludia.products.mosgis.ws.soap.tools;

import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import ru.gosuslugi.dom.schema.integration.base.HeaderType;

public class LoggingOutISRHMessageHandler extends LoggingOutMessageHandler implements SOAPHandler<SOAPMessageContext> {
    
    @Override
    final HeaderType createRequestHeader (SOAPMessageContext messageContext) {
        return of.createISRequestHeader ();
    }

}
