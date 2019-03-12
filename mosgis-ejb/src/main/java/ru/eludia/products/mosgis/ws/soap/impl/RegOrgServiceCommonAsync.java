package ru.eludia.products.mosgis.ws.soap.impl;

import com.sun.xml.ws.developer.SchemaValidation;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jms.Queue;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.ws.soap.impl.base.Fault;
import ru.eludia.products.mosgis.ws.soap.impl.base.WsInterceptor;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportOrgRegistryRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.GetStateResult;

@HandlerChain (file="handler-chain.xml")
@SchemaValidation(outbound = false)
@WebService(
    serviceName = "RegOrgServiceAsync", 
    portName = "RegOrgAsyncPort", 
    endpointInterface = "ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.RegOrgPortsTypeAsync", 
    targetNamespace = "http://dom.gosuslugi.ru/schema/integration/organizations-registry-common-service-async/", 
    wsdlLocation = "META-INF/wsdl/organizations-registry-common/hcs-organizations-registry-common-service-async.wsdl")
@Stateless
public class RegOrgServiceCommonAsync {

    @Resource
    private WebServiceContext wsContext;
    
    @Resource (mappedName = "mosgis.exportOrgRegistry")
    private Queue exportOrgRegistryQueue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @Interceptors(WsInterceptor.class)
    public GetStateResult getState(GetStateRequest getStateRequest) throws Fault {
        return null;
    }
    
    public AckRequest exportOrgRegistry(ExportOrgRegistryRequest exportOrgRegistryRequest) throws Fault {
        MessageContext msgContext = wsContext.getMessageContext();
        AckRequest.Ack ack = (AckRequest.Ack) msgContext.get("ack");
        AckRequest result = new AckRequest();
        result.setAck(ack);
        
        if (msgContext.get("isNew") != null && ((Boolean) msgContext.get("isNew")))
            UUIDPublisher.publish(exportOrgRegistryQueue, ack.getMessageGUID().toString());
        
        return result;
    }
    
}