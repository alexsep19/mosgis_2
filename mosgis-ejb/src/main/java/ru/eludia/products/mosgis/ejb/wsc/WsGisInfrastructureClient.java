package ru.eludia.products.mosgis.ejb.wsc;

import java.util.Map;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import ru.eludia.products.mosgis.db.model.tables.InfrastructureLog;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.base.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.infrastructure.GetStateResult;
import ru.gosuslugi.dom.schema.integration.infrastructure_service_async.InfrastructureServiceAsync;
import ru.gosuslugi.dom.schema.integration.infrastructure_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.infrastructure_service_async.InfrastructurePortsTypeAsync;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisInfrastructureClient {
    
    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/infrastructure/hcs-infrastructure-service-async.wsdl")
    private InfrastructureServiceAsync service;
    
    private InfrastructurePortsTypeAsync getPort (UUID orgPPAGuid, UUID messageGUID) {
        InfrastructurePortsTypeAsync port = service.getInfrastructurePortAsync ();
        VocSetting.setPort (port, "WS_GIS_INFRASTRUCTURES");
        final Map <String, Object> requestContext = ((BindingProvider)port).getRequestContext ();
        requestContext.put (LoggingOutMessageHandler.FIELD_MESSAGE_GUID, messageGUID);
        requestContext.put (LoggingOutMessageHandler.FIELD_ORG_PPA_GUID, orgPPAGuid);
        return port;
    }
    
    public GetStateResult getState (UUID orgPPAGuid, UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort (orgPPAGuid, UUID.randomUUID ()).getState (getStateRequest);
    }
    
    public AckRequest.Ack importOKI (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importOKI(InfrastructureLog.toImportOKIRequest (r)).getAck ();
    }
    
}
