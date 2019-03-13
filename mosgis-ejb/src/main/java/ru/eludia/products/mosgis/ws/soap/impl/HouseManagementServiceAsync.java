package ru.eludia.products.mosgis.ws.soap.impl;

import com.sun.xml.ws.developer.SchemaValidation;
import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import ru.eludia.products.mosgis.ws.soap.impl.base.BaseServiceAsync;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@HandlerChain (file="handler-chain.xml")
@SchemaValidation(outbound = false)
@WebService(
    serviceName = "HouseManagementServiceAsync", 
    portName = "HouseManagementPortAsync", 
    endpointInterface = "ru.gosuslugi.dom.schema.integration.house_management_service_async.HouseManagementPortsTypeAsync", 
    targetNamespace = "http://dom.gosuslugi.ru/schema/integration/house-management-service-async/", 
    wsdlLocation = "META-INF/wsdl/house-management/hcs-house-management-service-async.wsdl")
@Stateless
public class HouseManagementServiceAsync extends BaseServiceAsync {

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importSupplyResourceContractData (ru.gosuslugi.dom.schema.integration.house_management.ImportSupplyResourceContractRequest importSupplyResourceContractRequest) throws Fault {
        return publishIfNew (null);
    }    

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importMeteringDeviceData (ru.gosuslugi.dom.schema.integration.house_management.ImportMeteringDeviceDataRequest importMeteringDeviceDataRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportMeteringDeviceData (ru.gosuslugi.dom.schema.integration.house_management.ExportMeteringDeviceDataRequest exportMeteringDeviceDataRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.house_management.GetStateResult getState (ru.gosuslugi.dom.schema.integration.base.GetStateRequest getStateRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importContractData (ru.gosuslugi.dom.schema.integration.house_management.ImportContractRequest importContractRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importCharterData (ru.gosuslugi.dom.schema.integration.house_management.ImportCharterRequest importCharterRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportStatusCAChData (ru.gosuslugi.dom.schema.integration.house_management.ExportStatusCAChRequest exportStatusCAChRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportHouseData (ru.gosuslugi.dom.schema.integration.house_management.ExportHouseRequest exportHouseRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importAccountData (ru.gosuslugi.dom.schema.integration.house_management.ImportAccountRequest importAccountData) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportAccountData (ru.gosuslugi.dom.schema.integration.house_management.ExportAccountRequest exportAccountDataRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importPublicPropertyContract (ru.gosuslugi.dom.schema.integration.house_management.ImportPublicPropertyContractRequest importPublicPropertyContractRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportStatusPublicPropertyContract (ru.gosuslugi.dom.schema.integration.house_management.ExportStatusPublicPropertyContractRequest exportStatusPublicPropertyContractRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importNotificationData (ru.gosuslugi.dom.schema.integration.house_management.ImportNotificationRequest importNotificationRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importVotingProtocol (ru.gosuslugi.dom.schema.integration.house_management.ImportVotingProtocolRequest importVotingProtocolRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportVotingProtocol (ru.gosuslugi.dom.schema.integration.house_management.ExportVotingProtocolRequest exportVotingProtocolRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportCAChData (ru.gosuslugi.dom.schema.integration.house_management.ExportCAChAsyncRequest exportCAChDataRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importHouseUOData (ru.gosuslugi.dom.schema.integration.house_management.ImportHouseUORequest importHouseUODataRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importHouseRSOData (ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest importHouseRSODataRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importHouseOMSData (ru.gosuslugi.dom.schema.integration.house_management.ImportHouseOMSRequest importHouseOMSDataRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importHouseESPData (ru.gosuslugi.dom.schema.integration.house_management.ImportHouseESPRequest importHouseESPDataRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportSupplyResourceContractData (ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractRequest exportSupplyResourceContractRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importAccountIndividualServices (ru.gosuslugi.dom.schema.integration.house_management.ImportAccountIndividualServicesRequest importAccountIndividualServicesRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportAccountIndividualServices (ru.gosuslugi.dom.schema.integration.house_management.ExportAccountIndividualServicesRequest exportAccountIndividualServicesRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportSupplyResourceContractObjectAddressData (ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractObjectAddressRequest exportSupplyResourceContractObjectAddressRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importSupplyResourceContractObjectAddressData (ru.gosuslugi.dom.schema.integration.house_management.ImportSupplyResourceContractObjectAddressRequest importSupplyResourceContractObjectAddressRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest importSupplyResourceContractProjectData (ru.gosuslugi.dom.schema.integration.house_management.ImportSupplyResourceContractProjectRequest importSupplyResourceContractProjectRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportRolloverStatusCACh (ru.gosuslugi.dom.schema.integration.house_management.ExportRolloverStatusCAChRequest exportRolloverStatusCAChRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportBriefSupplyResourceContract (ru.gosuslugi.dom.schema.integration.house_management.ExportBriefSupplyResourceContractRequest exportBriefSupplyResourceContractRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportBriefSocialHireContract (ru.gosuslugi.dom.schema.integration.house_management.ExportBriefSocialHireContractRequest exportBriefSocialHireContractRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest demolishHouse (ru.gosuslugi.dom.schema.integration.house_management.DemolishHouseRequestType demolishHouseRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportBriefBasicHouse (ru.gosuslugi.dom.schema.integration.house_management.ExportBriefBasicHouseRequest exportBriefBasicHouseRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportBriefLivingHouse (ru.gosuslugi.dom.schema.integration.house_management.ExportBriefLivingHouseRequest exportBriefLivingHouseRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.base.AckRequest exportBriefApartmentHouse (ru.gosuslugi.dom.schema.integration.house_management.ExportBriefApartmentHouseRequest exportBriefApartmentHouseRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }
    
}
