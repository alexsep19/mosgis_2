package ru.eludia.products.mosgis.ws.soap.clients;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import ru.eludia.products.mosgis.db.model.voc.VocNsiListGroup;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.soap.tools.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiItemRequest;
import ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiListRequest;
import ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiPagingItemRequest;
import ru.gosuslugi.dom.schema.integration.nsi_common.GetStateResult;
import ru.gosuslugi.dom.schema.integration.nsi_common_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.nsi_common_service_async.NsiPortsTypeAsync;
import ru.gosuslugi.dom.schema.integration.nsi_common_service_async.NsiServiceAsync;
import ru.gosuslugi.dom.schema.integration.nsi_common.ObjectFactory;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisNsiCommonClient {
    
    private static final ObjectFactory of = new ObjectFactory ();
    
    @HandlerChain (file="handler-chain-out-anon.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/nsi-common/hcs-nsi-common-service-async.wsdl")
    private NsiServiceAsync service;
        
    private NsiPortsTypeAsync getPort (UUID messageGUID) {
        ru.gosuslugi.dom.schema.integration.nsi_common_service_async.NsiPortsTypeAsync port = service.getNsiPortAsync();
        VocSetting.setPort (port, "WS_GIS_NSI_COMMON");
        final Map<String, Object> requestContext = ((BindingProvider)port).getRequestContext ();
        requestContext.put (LoggingOutMessageHandler.FIELD_MESSAGE_GUID, messageGUID);
        return port;
    }

    public AckRequest.Ack exportNsiList (VocNsiListGroup.i listGroup, UUID messageGUID) throws Fault {
        ExportNsiListRequest rq = of.createExportNsiListRequest ();
        rq.setListGroup (listGroup.getName ());
        return getPort (messageGUID).exportNsiList (rq).getAck ();
    }

    public AckRequest.Ack exportNsiItem (VocNsiListGroup.i listGroup, int registryNumber, UUID messageGUID) throws Fault {
        ExportNsiItemRequest rq = of.createExportNsiItemRequest ();
        rq.setListGroup (listGroup.getName ());
        rq.setRegistryNumber (BigInteger.valueOf (registryNumber));
        return getPort (messageGUID).exportNsiItem (rq).getAck ();
    }
 
    public AckRequest.Ack exportNsiPagingItem (VocNsiListGroup.i listGroup, int registryNumber, int page, UUID messageGUID) throws Fault {
        ExportNsiPagingItemRequest rq = of.createExportNsiPagingItemRequest ();
        rq.setListGroup (listGroup.getName ());
        rq.setRegistryNumber (BigInteger.valueOf (registryNumber));
        rq.setPage (page);
        return getPort (messageGUID).exportNsiPagingItem (rq).getAck ();
    }

    public GetStateResult getState (UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort (UUID.randomUUID ()).getState (getStateRequest);
    }
    
}