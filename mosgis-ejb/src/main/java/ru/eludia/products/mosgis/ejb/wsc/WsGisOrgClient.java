package ru.eludia.products.mosgis.ejb.wsc;

import java.util.UUID;
import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.xml.ws.WebServiceRef;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportOrgRegistryRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.GetStateResult;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.RegOrgPortsTypeAsync;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.RegOrgServiceAsync;

@Stateless
public class WsGisOrgClient {

    private static final ObjectFactory of = new ObjectFactory ();
    
    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/organizations-registry-common/hcs-organizations-registry-common-service-async.wsdl")
    private RegOrgServiceAsync service;

    private RegOrgPortsTypeAsync getPort () {
        RegOrgPortsTypeAsync port = service.getRegOrgAsyncPort ();
        VocSetting.setPort (port, "WS_GIS_ORG_COMMON");
        return port;
    }

    public GetStateResult getState (UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort ().getState (getStateRequest);
    }    

    public AckRequest.Ack exportOrgRegistry (String orgn) throws Fault {
        
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
        return getPort ().exportOrgRegistry (r).getAck ();
        
    }

}