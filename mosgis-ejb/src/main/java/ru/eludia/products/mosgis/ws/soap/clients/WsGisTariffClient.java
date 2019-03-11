package ru.eludia.products.mosgis.ws.soap.clients;

import java.util.Map;
import java.util.UUID;
//import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.soap.tools.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.tariff.ExportTariffDifferentiationRequest;
import ru.gosuslugi.dom.schema.integration.tariff.GetStateResult;
import ru.gosuslugi.dom.schema.integration.tariff_service.Fault;
import ru.gosuslugi.dom.schema.integration.tariff_service.TariffAsyncPort;
import ru.gosuslugi.dom.schema.integration.tariff_service.TariffAsyncService;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisTariffClient {
        
//    private static final Logger logger = Logger.getLogger (WsGisServicesClient.class.getName ());
    
    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/tariff/hcs-tariff-service-async.wsdl")
    
    private TariffAsyncService service;

    private TariffAsyncPort getPort (UUID orgPPAGuid, UUID messageGUID) {
        TariffAsyncPort port = service.getTariffAsyncPort ();
        VocSetting.setPort (port, "WS_GIS_TARIFF");
        final Map<String, Object> requestContext = ((BindingProvider)port).getRequestContext ();
        requestContext.put (LoggingOutMessageHandler.FIELD_MESSAGE_GUID, messageGUID);
        requestContext.put (LoggingOutMessageHandler.FIELD_ORG_PPA_GUID, orgPPAGuid);
        return port;
    }
    
    public GetStateResult getState (UUID orgPPAGuid, UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort (orgPPAGuid, UUID.randomUUID ()).getState (getStateRequest);
    }
    
    public AckRequest.Ack exportTariffDifferentiation (UUID orgPPAGuid, UUID messageGUID) throws Fault {
        final ExportTariffDifferentiationRequest rq = new ExportTariffDifferentiationRequest ();
        return getPort (orgPPAGuid, messageGUID).exportTariffDifferentiation (rq).getAck ();
    }
    
}