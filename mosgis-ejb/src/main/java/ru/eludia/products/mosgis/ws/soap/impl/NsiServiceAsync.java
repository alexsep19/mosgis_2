package ru.eludia.products.mosgis.ws.soap.impl;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jms.Queue;
import javax.jws.HandlerChain;
import javax.jws.WebService;

import com.sun.xml.ws.developer.SchemaValidation;

import ru.eludia.products.mosgis.ws.soap.impl.base.BaseServiceAsync;
import ru.eludia.products.mosgis.ws.soap.impl.base.WsInterceptor;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.Fault;

/**
 * 
 * @author Aleksei
 *
 */
@HandlerChain(file = "handler-chain.xml")
@SchemaValidation(outbound = false)
@WebService(
		serviceName = "NsiServiceAsync", 
		portName = "NsiPortAsync", 
		endpointInterface = "ru.gosuslugi.dom.schema.integration.nsi_service_async.NsiPortsTypeAsync", 
		targetNamespace = "http://dom.gosuslugi.ru/schema/integration/nsi-service-async/", 
		wsdlLocation = "META-INF/wsdl/nsi/hcs-nsi-service-async.wsdl")
@Stateless
public class NsiServiceAsync extends BaseServiceAsync {

	@Resource (mappedName = "mosgis.importCapitalRepairWork")
    private Queue importCapitalRepairWorkQueue;
	
	public ru.gosuslugi.dom.schema.integration.base.AckRequest importAdditionalServices(ru.gosuslugi.dom.schema.integration.nsi.ImportAdditionalServicesRequest importAdditionalServicesRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importMunicipalServices(ru.gosuslugi.dom.schema.integration.nsi.ImportMunicipalServicesRequest importMunicipalServicesRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importOrganizationWorks(ru.gosuslugi.dom.schema.integration.nsi.ImportOrganizationWorksRequest importOrganizationWorksRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importCommunalInfrastructureSystem(ru.gosuslugi.dom.schema.integration.nsi.ImportCommunalInfrastructureSystemRequest importCommunalInfrastructureSystemRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Interceptors(WsInterceptor.class)
    public ru.gosuslugi.dom.schema.integration.nsi.GetStateResult getState(ru.gosuslugi.dom.schema.integration.base.GetStateRequest getStateRequest) throws Fault {
    	return null;
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportDataProviderNsiItem(ru.gosuslugi.dom.schema.integration.nsi.ExportDataProviderNsiItemRequest exportDataProviderNsiItemRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportDataProviderPagingNsiItem(ru.gosuslugi.dom.schema.integration.nsi.ExportDataProviderNsiPagingItemRequest exportDataProviderNsiPagingItemRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importCapitalRepairWork(ru.gosuslugi.dom.schema.integration.nsi.ImportCapitalRepairWorkRequest importCapitalRepairWorkRequest) throws Fault {
    	 return publishIfNew (importCapitalRepairWorkQueue);
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importBaseDecisionMSP(ru.gosuslugi.dom.schema.integration.nsi.ImportBaseDecisionMSPRequest importBaseDecisionMSPRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importGeneralNeedsMunicipalResource(ru.gosuslugi.dom.schema.integration.nsi.ImportGeneralNeedsMunicipalResourceRequest importGeneralNeedsMunicipalResourceRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }
    
}
