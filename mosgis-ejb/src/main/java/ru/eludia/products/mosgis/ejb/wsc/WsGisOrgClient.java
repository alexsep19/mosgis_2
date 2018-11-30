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
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportDelegatedAccessRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportOrgRegistryRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.GetStateResult;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.RegOrgPortsTypeAsync;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.RegOrgServiceAsync;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisOrgClient {

    private static final ObjectFactory of = new ObjectFactory ();
    
    @HandlerChain (file="handler-chain-out-anon.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/organizations-registry-common/hcs-organizations-registry-common-service-async.wsdl")
    private RegOrgServiceAsync service;

    private RegOrgPortsTypeAsync getPort () {
        RegOrgPortsTypeAsync port = service.getRegOrgAsyncPort ();
        VocSetting.setPort (port, "WS_GIS_ORG_COMMON");
        return port;
    }
    
    private RegOrgPortsTypeAsync getPort (UUID messageGUID) {
        RegOrgPortsTypeAsync port = service.getRegOrgAsyncPort ();
        VocSetting.setPort (port, "WS_GIS_ORG_COMMON");
        final Map<String, Object> requestContext = ((BindingProvider)port).getRequestContext ();
        requestContext.put (LoggingOutMessageHandler.FIELD_MESSAGE_GUID, messageGUID);
        return port;
    }
    
    

    public GetStateResult getState (UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort ().getState (getStateRequest);
    }    

    
    public AckRequest.Ack exportOrgRegistry (String orgn, UUID messageGUID) throws Fault {
        
        if (orgn == null) throw new IllegalArgumentException ("Null OGRN passed");
        
        ExportOrgRegistryRequest.SearchCriteria s = of.createExportOrgRegistryRequestSearchCriteria ();
        
        switch (orgn.length ()) {
            case 13:
                s.setOGRN (orgn);
                break;
            case 15: 
                s.setOGRNIP (orgn);
                break;
            default: throw new IllegalArgumentException ("Wrong OGRN passed: '" + orgn + "'");
        }
        
        ExportOrgRegistryRequest r = of.createExportOrgRegistryRequest ();
        r.getSearchCriteria ().add (s);        
        return getPort (messageGUID).exportOrgRegistry (r).getAck ();
        
    }
    
    public AckRequest.Ack exportOrgRegistry (UUID orgrootentityguid, UUID messageGUID) throws Fault {

        ExportOrgRegistryRequest.SearchCriteria s = of.createExportOrgRegistryRequestSearchCriteria ();
        s.setOrgRootEntityGUID (orgrootentityguid.toString ());

        ExportOrgRegistryRequest r = of.createExportOrgRegistryRequest ();
        r.getSearchCriteria ().add (s);        
        return getPort (messageGUID).exportOrgRegistry (r).getAck ();

    }

    public AckRequest.Ack exportDelegatedAccess (int page, UUID messageGUID) throws Fault {
        final ExportDelegatedAccessRequest createExportDelegatedAccessRequest = of.createExportDelegatedAccessRequest ();
        if (page > 0) createExportDelegatedAccessRequest.setPage (page);
        return getPort (messageGUID).exportDelegatedAccess (createExportDelegatedAccessRequest).getAck ();
    }

}