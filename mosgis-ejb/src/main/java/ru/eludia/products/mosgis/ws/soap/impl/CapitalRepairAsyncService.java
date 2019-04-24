package ru.eludia.products.mosgis.ws.soap.impl;

import com.sun.xml.ws.developer.SchemaValidation;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jms.Queue;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import ru.eludia.products.mosgis.ws.soap.impl.base.BaseServiceAsync;
import ru.eludia.products.mosgis.ws.soap.impl.base.WsInterceptor;
import ru.gosuslugi.dom.schema.integration.capital_repair_service_async.Fault;

@HandlerChain (file="handler-chain.xml")
@SchemaValidation(outbound = false)
@WebService(
    serviceName = "CapitalRepairAsyncService",
    portName = "CapitalRepairAsyncPort",
    endpointInterface = "ru.gosuslugi.dom.schema.integration.capital_repair_service_async.CapitalRepairAsyncPort",
    targetNamespace = "http://dom.gosuslugi.ru/schema/integration/capital-repair-service-async/",
    wsdlLocation = "META-INF/wsdl/capital-repair/hcs-capital-repair-service-async.wsdl")
@Stateless
public class CapitalRepairAsyncService extends BaseServiceAsync {

@Resource (mappedName = "mosgis.inSoapImportRegionalOperatorAccounts")
    private Queue inSoapImportRegionalOperatorAccounts;

    @Interceptors(WsInterceptor.class)
    public ru.gosuslugi.dom.schema.integration.capital_repair.GetStateResult getState(ru.gosuslugi.dom.schema.integration.base.GetStateRequest getStateRequest) throws Fault {
	return null;
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importContracts(ru.gosuslugi.dom.schema.integration.capital_repair.ImportContractsRequest importContractsRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportContracts(ru.gosuslugi.dom.schema.integration.capital_repair.ExportContractsRequest exportContractsRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importRegionalProgram(ru.gosuslugi.dom.schema.integration.capital_repair.ImportRegionalProgramRequest importRegionalProgramRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importRegionalProgramWork(ru.gosuslugi.dom.schema.integration.capital_repair.ImportRegionalProgramWorkRequest importRegionalProgramWorkRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportRegionalProgram(ru.gosuslugi.dom.schema.integration.capital_repair.ExportRegionalProgramRequest exportRegionalProgramRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportRegionalProgramWork(ru.gosuslugi.dom.schema.integration.capital_repair.ExportRegionalProgramWorkRequest exportRegionalProgramWorkRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importPlan(ru.gosuslugi.dom.schema.integration.capital_repair.ImportPlanRequest importPlanRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importPlanWork(ru.gosuslugi.dom.schema.integration.capital_repair.ImportPlanWorkRequest importPlanWorkRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportPlan(ru.gosuslugi.dom.schema.integration.capital_repair.ExportPlanRequest exportPlanRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportPlanWork(ru.gosuslugi.dom.schema.integration.capital_repair.ExportPlanWorkRequest exportPlanWorkRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importRegionalOperatorAccounts(ru.gosuslugi.dom.schema.integration.capital_repair.ImportAccountRegionalOperatorRequest importAccountRegionalOperatorRequest) throws Fault {
	return publishIfNew(inSoapImportRegionalOperatorAccounts);
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importSpecialAccounts(ru.gosuslugi.dom.schema.integration.capital_repair.ImportAccountSpecialRequest importAccountSpecialRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportAccounts(ru.gosuslugi.dom.schema.integration.capital_repair.ExportAccountRequest exportAccountRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importDecisionsFormingFund(ru.gosuslugi.dom.schema.integration.capital_repair.ImportDecisionsFormingFundRequest importDecisionsFormingFundRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportDecisionsFormingFund(ru.gosuslugi.dom.schema.integration.capital_repair.ExportDecisionsFormingFundRequest exportDecisionsFormingFundRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importCertificates(ru.gosuslugi.dom.schema.integration.capital_repair.ImportCertificatesRequest importCertificatesRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importPaymentsInfo(ru.gosuslugi.dom.schema.integration.capital_repair.ImportPaymentsInfoRequest importPaymentsInfoRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importOperationAndBalance(ru.gosuslugi.dom.schema.integration.capital_repair.ImportOperationAndBalanceRequest importOperationAndBalanceRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportCreditContract(ru.gosuslugi.dom.schema.integration.capital_repair.ExportCreditContractRequest exportCreditContractRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importFundSizeInfo(ru.gosuslugi.dom.schema.integration.capital_repair.ImportFundSizeInfoRequest importFundSizeInfoRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportFundSizeInfo(ru.gosuslugi.dom.schema.integration.capital_repair.ExportFundSizeInfoRequest exportFundSizeInfoRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importDecisionsOrderOfProvidingPD(ru.gosuslugi.dom.schema.integration.capital_repair.ImportDecisionsOrderOfProvidingPDRequest importDecisionsOrderOfProvidingPDRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportDecisionsOrderOfProvidingPD(ru.gosuslugi.dom.schema.integration.capital_repair.ExportDecisionsOrderOfProvidingPDRequest exportDecisionsOrderOfProvidingPDRequest) throws Fault {
	//TODO implement this method
	throw new UnsupportedOperationException("Not implemented yet.");
    }

}
