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
import ru.eludia.products.mosgis.db.model.tables.PaymentLog;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.soap.tools.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.payment.GetStateResult;
import ru.gosuslugi.dom.schema.integration.payment_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.payment_service_async.PaymentPortsTypeAsync;
import ru.gosuslugi.dom.schema.integration.payment_service_async.PaymentsServiceAsync;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisPaymentClient {
            
    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/payment/hcs-payment-service-async.wsdl")
    
    private PaymentsServiceAsync service;

    private PaymentPortsTypeAsync getPort (UUID orgPPAGuid, UUID messageGUID) {
        PaymentPortsTypeAsync port = service.getPaymentPortAsync();
        VocSetting.setPort (port, "WS_GIS_PAYMENT");
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

    public AckRequest.Ack importSupplierNotificationsOfOrderExecution(UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
	return getPort(orgPPAGuid, messageGUID).importSupplierNotificationsOfOrderExecution(PaymentLog.toImportSupplierNotificationsOfOrderExecutionRequest(r)).getAck();
    }
}