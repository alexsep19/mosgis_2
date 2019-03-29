package ru.eludia.products.mosgis.ws.soap.clients;

import java.util.Map;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;

import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.soap.tools.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.inspection.ExportInspectionPlansRequest;
import ru.gosuslugi.dom.schema.integration.inspection.GetStateResult;
import ru.gosuslugi.dom.schema.integration.inspection.ImportInspectionPlanRequest;
import ru.gosuslugi.dom.schema.integration.inspection.ImportInspectionPlanRequest.ImportInspectionPlan;
import ru.gosuslugi.dom.schema.integration.inspection_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.inspection_service_async.InspectionPortsTypeAsync;
import ru.gosuslugi.dom.schema.integration.inspection_service_async.InspectionServiceAsync;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisInspectionClient {
    
    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/inspection/hcs-inspection-service-async.wsdl")
    private InspectionServiceAsync service;  

	private InspectionPortsTypeAsync getPort(UUID orgPPAGuid, UUID messageGUID) {
		InspectionPortsTypeAsync port = service.getInspectionPortAsync();
		VocSetting.setPort(port, "WS_GIS_INSPECTION");
		final Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
		requestContext.put(LoggingOutMessageHandler.FIELD_MESSAGE_GUID, messageGUID);
		requestContext.put(LoggingOutMessageHandler.FIELD_ORG_PPA_GUID, orgPPAGuid);
		return port;
	}

    public GetStateResult getState (UUID orgPPAGuid, UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort (orgPPAGuid, UUID.randomUUID()).getState (getStateRequest);
    }
    
    public AckRequest.Ack exportInspectionPlans(UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
    	ExportInspectionPlansRequest request = new ExportInspectionPlansRequest();
    	
    	if (!r.isEmpty()) {
    		if (r.get("yearfrom") != null)
    			request.setYearFrom(Short.valueOf(r.get("yearfrom").toString()));
    		if (r.get("yearto") != null)
    			request.setYearTo(Short.valueOf(r.get("yearto").toString()));
    	}
    	
    	return getPort(orgPPAGuid, messageGUID).exportInspectionPlans(request).getAck();
    }
    
    public AckRequest.Ack importInspectionPlan(UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
    	ImportInspectionPlanRequest request = new ImportInspectionPlanRequest();
    	request.getImportInspectionPlan().add(TypeConverter.javaBean(ImportInspectionPlan.class, r));
    	
    	return getPort(orgPPAGuid, messageGUID).importInspectionPlan(request).getAck();
    }

}
