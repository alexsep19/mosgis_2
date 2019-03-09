package ru.eludia.products.mosgis.ws.soap.clients;

import java.util.Map;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.soap.tools.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.licenses.ExportLicenseRequest;
import ru.gosuslugi.dom.schema.integration.licenses.GetStateResult;
import ru.gosuslugi.dom.schema.integration.licenses.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.licenses_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.licenses_service_async.LicensePortsTypeAsync;
import ru.gosuslugi.dom.schema.integration.licenses_service_async.LicenseServiceAsync;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisLicenseClient {

    private static final ObjectFactory of = new ObjectFactory ();
    
    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/licenses/hcs-licenses-service-async.wsdl")
    private LicenseServiceAsync service;
    
    private LicensePortsTypeAsync getPort(UUID orgPPAGuid, UUID messageGUID) {
        LicensePortsTypeAsync port = service.getLicensesPortAsync();
        VocSetting.setPort(port, "WS_GIS_LICENSES");
        final Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
        requestContext.put(LoggingOutMessageHandler.FIELD_MESSAGE_GUID, messageGUID);
        requestContext.put(LoggingOutMessageHandler.FIELD_ORG_PPA_GUID, orgPPAGuid);
        return port;
    }

    public GetStateResult getState(UUID orgPPAGuid, UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest();
        getStateRequest.setMessageGUID(uuid.toString());
        return getPort(orgPPAGuid, UUID.randomUUID()).getState(getStateRequest);
    }

    
    public AckRequest.Ack exportLicenses(String ogrn, UUID orgPPAGuid, UUID messageGUID) throws Fault {

        if (ogrn == null) {
            throw new IllegalArgumentException("Null OGRN passed");
        }

        ExportLicenseRequest.LicenseOrganization org = of.createExportLicenseRequestLicenseOrganization();

        switch (ogrn.length()) {
            case 13:
                org.setOGRN(ogrn);
                break;
            case 15:
                org.setOGRNIP(ogrn);
                break;
            default:
                throw new IllegalArgumentException("Wrong OGRN passed: '" + ogrn + "'");
        }

        ExportLicenseRequest request = of.createExportLicenseRequest();
        request.getLicenseOrganization().add(org);
        return getPort(orgPPAGuid, messageGUID).exportLicense(request).getAck();

    }

}