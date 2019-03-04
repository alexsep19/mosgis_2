package ru.eludia.products.mosgis.ejb.wsc;

import java.util.Map;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.base.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.device_metering.GetStateResult;
import ru.gosuslugi.dom.schema.integration.device_metering_service_async.DeviceMeteringServiceAsync;
import ru.gosuslugi.dom.schema.integration.device_metering_service_async.DeviceMeteringPortTypesAsync;
import ru.gosuslugi.dom.schema.integration.device_metering_service_async.Fault;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisMeteringClient {

    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/device-metering/hcs-device-metering-service-async.wsdl")
    private DeviceMeteringServiceAsync service;
    
    private DeviceMeteringPortTypesAsync getPort (UUID orgPPAGuid, UUID messageGUID) {
        DeviceMeteringPortTypesAsync port = service.getDeviceMeteringPortAsync ();
        VocSetting.setPort (port, "WS_GIS_METERING");
        final Map<String, Object> requestContext = ((BindingProvider)port).getRequestContext ();
        requestContext.put (LoggingOutMessageHandler.FIELD_MESSAGE_GUID, messageGUID);
        requestContext.put (LoggingOutMessageHandler.FIELD_ORG_PPA_GUID, orgPPAGuid);
        return port;
    }
    
    public GetStateResult getState (UUID orgPPAGuid, UUID messageGUID) throws Fault {
        final GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (messageGUID.toString ());
        return getPort (orgPPAGuid, messageGUID).getState (getStateRequest);
    }

    public AckRequest.Ack importMeteringDeviceValues (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importMeteringDeviceValues (null).getAck ();
    }
    
}
