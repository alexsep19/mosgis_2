package ru.eludia.products.mosgis.ws.soap.clients;

import java.util.Map;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import ru.eludia.products.mosgis.db.model.tables.LegalActLog;

import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.soap.tools.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.uk.GetStateResult;
import ru.gosuslugi.dom.schema.integration.uk_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.uk_service_async.UkAsyncPort;
import ru.gosuslugi.dom.schema.integration.uk_service_async.UkAsyncService;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisUkClient {
    
    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/uk/hcs-uk-service-async.wsdl")
    private UkAsyncService service;

    private UkAsyncPort getPort(UUID orgPPAGuid, UUID messageGUID) {
	UkAsyncPort port = service.getUkAsyncPort();
	VocSetting.setPort(port, "WS_GIS_UK");
	final Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
	requestContext.put(LoggingOutMessageHandler.FIELD_MESSAGE_GUID, messageGUID);
	requestContext.put(LoggingOutMessageHandler.FIELD_ORG_PPA_GUID, orgPPAGuid);
	return port;
    }

    public GetStateResult getState(UUID orgPPAGuid, UUID uuid) throws Fault {
	GetStateRequest getStateRequest = new GetStateRequest();
	getStateRequest.setMessageGUID(uuid.toString());
	return getPort(orgPPAGuid, UUID.randomUUID()).getState(getStateRequest);
    }

    public AckRequest.Ack importDocumentsMunicipal(UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
	return getPort(orgPPAGuid, messageGUID).importDocumentsMunicipal(LegalActLog.toImportDocumentsMunicipalRequest(r)).getAck();
    }

    public AckRequest.Ack importDocumentsRegion(UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
	return getPort(orgPPAGuid, messageGUID).importDocumentsRegion(LegalActLog.toImportDocumentsRegionRequest(r)).getAck();
    }
}
