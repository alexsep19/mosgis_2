package ru.eludia.products.mosgis.ws.soap.clients;

import java.util.Map;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationLog;

import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.soap.tools.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.msp.ExportCategoriesRequest;
import ru.gosuslugi.dom.schema.integration.msp.GetStateResult;
import ru.gosuslugi.dom.schema.integration.msp_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.msp_service_async.MSPAsyncPort;
import ru.gosuslugi.dom.schema.integration.msp_service_async.MSPAsyncService;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisMSPClient {
    
    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/msp/hcs-msp-service-async.wsdl")
    private MSPAsyncService service;

    private MSPAsyncPort getPort(UUID orgPPAGuid, UUID messageGUID) {
	MSPAsyncPort port = service.getMSPAsyncPort();
	VocSetting.setPort(port, "WS_GIS_MSP");
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

    public AckRequest.Ack exportCategories(UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {

	ExportCategoriesRequest request = DB.to.javaBean(ExportCategoriesRequest.class, r);

	return getPort(orgPPAGuid, messageGUID).exportCategories(request).getAck();
    }

    public AckRequest.Ack importCitizenCompensation(UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {

	return getPort(orgPPAGuid, messageGUID).importCitizenCompensation(CitizenCompensationLog.toImportCitizenCompensationRequest(r)).getAck();
    }
    
}
