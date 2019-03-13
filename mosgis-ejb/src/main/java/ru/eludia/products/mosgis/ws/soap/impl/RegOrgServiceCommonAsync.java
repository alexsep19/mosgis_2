package ru.eludia.products.mosgis.ws.soap.impl;

import com.sun.xml.ws.developer.SchemaValidation;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jms.Queue;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import ru.eludia.products.mosgis.ws.soap.impl.base.BaseServiceAsync;
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
public class RegOrgServiceCommonAsync extends BaseServiceAsync {
    
    @Resource (mappedName = "mosgis.exportOrgRegistry")
    private Queue exportOrgRegistryQueue;
       
    @Interceptors(WsInterceptor.class)
    public GetStateResult getState(GetStateRequest getStateRequest) throws Fault {
        return null;
    }
    
    public AckRequest exportOrgRegistry (ExportOrgRegistryRequest exportOrgRegistryRequest) throws Fault {
        return publishIfNew (exportOrgRegistryQueue);
    }
    
    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportDataProvider (ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportDataProviderRequest exportDataProviderRequest) throws ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportDelegatedAccess (ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportDelegatedAccessRequest exportDelegatedAccessRequest) throws ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportObjectsDelegatedAccess (ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportObjectsDelegatedAccessRequest exportObjectsDelegatedAccessRequest) throws ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportTerritoryDelegatedAccess (ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportTerritoryDelegatedAccessRequest exportTerritoryDelegatedAccessRequest) throws ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }
        
}