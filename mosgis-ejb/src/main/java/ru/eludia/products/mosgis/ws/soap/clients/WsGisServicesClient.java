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
import ru.eludia.products.mosgis.db.model.tables.ReportingPeriodLog;
import ru.eludia.products.mosgis.db.model.tables.WorkingListLog;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlanLog;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.soap.tools.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.services.GetStateResult;
import ru.gosuslugi.dom.schema.integration.services_service_async.ServicesServiceAsync;
import ru.gosuslugi.dom.schema.integration.services_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.services_service_async.ServicesPortsTypeAsync;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisServicesClient {
        
//    private static final Logger logger = Logger.getLogger (WsGisServicesClient.class.getName ());
    
    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/services/hcs-services-service-async.wsdl")
    private ServicesServiceAsync service;

    private ServicesPortsTypeAsync getPort (UUID orgPPAGuid, UUID messageGUID) {
        ServicesPortsTypeAsync port = service.getDefaultPort ();
        VocSetting.setPort (port, "WS_GIS_SERVICES");
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
    
    public AckRequest.Ack importWorkingList (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importWorkingList (WorkingListLog.toImportWorkingListRequest (r)).getAck ();
    }
        
    public AckRequest.Ack exportWorkingList (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).exportWorkingList (WorkingListLog.toExportWorkingListRequest (r)).getAck ();
    }

    public AckRequest.Ack cancelWorkingList (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importWorkingList (WorkingListLog.toCancelImportWorkingListRequest (r)).getAck ();
    }
    
    public AckRequest.Ack annulWorkingList (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importWorkingList (WorkingListLog.toAnnulImportWorkingListRequest (r)).getAck ();
    }
    
    public AckRequest.Ack importWorkingPlan (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importWorkingPlan (WorkingPlanLog.toImportWorkingPlanRequest (r)).getAck ();
    }    
    
    public AckRequest.Ack exportWorkingPlan (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).exportWorkingPlan (WorkingPlanLog.toExportWorkingPlanRequest (r)).getAck ();
    }

    public AckRequest.Ack importCompletedWorks (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importCompletedWorks (ReportingPeriodLog.toCompletedWorksRequest (r)).getAck ();
    }    
    
}