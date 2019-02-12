package ru.eludia.products.mosgis.web;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import ru.mos.gkh.gis.schema.integration.base.AckRequest;
import ru.mos.gkh.gis.schema.integration.base.GetStateRequest;
import ru.mos.gkh.gis.schema.integration.organizations_registry_common.ExportOrgRegistryRequest;
import ru.mos.gkh.gis.schema.integration.organizations_registry_common.GetStateResult;
import ru.mos.gkh.gis.schema.integration.organizations_registry_common_service_async.Fault;

/**
 *
 * @author Aleksei
 */
@HandlerChain (file="handler-chain.xml")
@WebService(
        serviceName = "RegOrgServiceAsync", 
        portName = "RegOrgAsyncPort", 
        endpointInterface = "ru.mos.gkh.gis.schema.integration.organizations_registry_common_service_async.RegOrgPortsTypeAsync", 
        targetNamespace = "http://gis.gkh.mos.ru/schema/integration/organizations-registry-common-service-async/", 
        wsdlLocation = "META-INF/ws/wsdl/organizations-registry-common/hcs-organizations-registry-common-service-async.wsdl")
@Stateless
public class RegOrgServiceCommonAsync {

    @Resource
    public WebServiceContext wsContext;
    
    public GetStateResult getState(GetStateRequest getStateRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }
    
    public AckRequest exportOrgRegistry(ExportOrgRegistryRequest exportOrgRegistryRequest) throws Fault {
        MessageContext msgContext = wsContext.getMessageContext();
        AckRequest.Ack ack = (AckRequest.Ack) msgContext.get("ack");
        AckRequest result = new AckRequest();
        result.setAck(ack);
        return result;
    }
    
}
