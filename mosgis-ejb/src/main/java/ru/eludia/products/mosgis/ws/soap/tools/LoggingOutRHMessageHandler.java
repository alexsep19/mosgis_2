package ru.eludia.products.mosgis.ws.soap.tools;

import java.util.UUID;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.jmx.Conf;
import static ru.eludia.products.mosgis.ws.soap.tools.LoggingOutMessageHandler.FIELD_ORG_PPA_GUID;
import ru.gosuslugi.dom.schema.integration.base.HeaderType;
import ru.gosuslugi.dom.schema.integration.base.RequestHeader;

public class LoggingOutRHMessageHandler extends LoggingOutMessageHandler implements SOAPHandler<SOAPMessageContext> {
    
    private UUID getOrgPPAGuid (SOAPMessageContext messageContext) {
        UUID uuid = (UUID) messageContext.get (FIELD_ORG_PPA_GUID);
        return uuid == null ? UUID.fromString (Conf.get (VocSetting.i.GIS_ID_ORGANIZATION)) : uuid;
    }

    @Override
    final HeaderType createRequestHeader (SOAPMessageContext messageContext) {
        RequestHeader rh = of.createRequestHeader ();
        rh.setOrgPPAGUID (getOrgPPAGuid (messageContext).toString ());
        rh.setIsOperatorSignature (Boolean.TRUE);
        return rh;
    }

    @Override
    String getOrgPPAGUID (HeaderType rh) {
        return ((RequestHeader) rh).getOrgPPAGUID ();
    }

}