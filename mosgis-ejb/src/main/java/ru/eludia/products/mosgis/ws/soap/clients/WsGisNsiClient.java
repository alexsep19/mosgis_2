package ru.eludia.products.mosgis.ws.soap.clients;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.soap.tools.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.nsi.ExportDataProviderNsiItemRequest;
import ru.gosuslugi.dom.schema.integration.nsi.GetStateResult;
import ru.gosuslugi.dom.schema.integration.nsi.ImportAdditionalServicesRequest;
import ru.gosuslugi.dom.schema.integration.nsi.ImportMunicipalServicesRequest;
import ru.gosuslugi.dom.schema.integration.nsi.ImportOrganizationWorkType;
import ru.gosuslugi.dom.schema.integration.nsi.ImportOrganizationWorksRequest;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.NsiPortsTypeAsync;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.NsiServiceAsync;
import ru.gosuslugi.dom.schema.integration.nsi.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisNsiClient {
    
    private static final ObjectFactory of = new ObjectFactory ();
    
    private static final Logger logger = Logger.getLogger (WsGisNsiClient.class.getName ());
    
    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/nsi/hcs-nsi-service-async.wsdl")
    private NsiServiceAsync service;
        
    private NsiPortsTypeAsync getPort (UUID orgPPAGuid) {
        ru.gosuslugi.dom.schema.integration.nsi_service_async.NsiPortsTypeAsync port = service.getNsiPortAsync();
        VocSetting.setPort (port, "WS_GIS_NSI");
        ((BindingProvider)port).getRequestContext ().put (LoggingOutMessageHandler.FIELD_ORG_PPA_GUID, orgPPAGuid);
        return port;
    }
    
    private NsiPortsTypeAsync getPort (UUID orgPPAGuid, UUID messageGUID) {
        ru.gosuslugi.dom.schema.integration.nsi_service_async.NsiPortsTypeAsync port = service.getNsiPortAsync();
        VocSetting.setPort (port, "WS_GIS_NSI");
        ((BindingProvider)port).getRequestContext ().put (LoggingOutMessageHandler.FIELD_MESSAGE_GUID, messageGUID);
        ((BindingProvider)port).getRequestContext ().put (LoggingOutMessageHandler.FIELD_ORG_PPA_GUID, orgPPAGuid);
        return port;
    }

    public GetStateResult getState (UUID orgPPAGuid, UUID uuid) throws Fault {
        if (orgPPAGuid == null) throw new NullPointerException ();
        GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort (orgPPAGuid).getState (getStateRequest);
    }           
    
    public AckRequest.Ack importOrganizationWorks (UUID orgPPAGuid, Map<String, Object> r) throws Fault {
        
        ImportOrganizationWorksRequest rq = of.createImportOrganizationWorksRequest ();
        
        final String action = r.get ("action").toString ();
        
        UUID tr = UUID.randomUUID ();

        switch (action) {
            case "delete":
                ImportOrganizationWorksRequest.DeleteOrganizationWork d = (ImportOrganizationWorksRequest.DeleteOrganizationWork) DB.to.javaBean (ImportOrganizationWorksRequest.DeleteOrganizationWork.class, r);
                d.setTransportGUID (tr.toString ());
                rq.getDeleteOrganizationWork ().add (d);
                break;
            case "create":
            case "update":
                ImportOrganizationWorkType e = (ImportOrganizationWorkType) DB.to.javaBean (ImportOrganizationWorkType.class, r);
                e.setServiceTypeRef (NsiTable.toDom ((String) r.get ("code_vc_nsi_56"), (UUID) r.get ("vc_nsi_56.guid")));
                e.getRequiredServiceRef ().addAll ((List <NsiRef>) r.get ("codes_vc_nsi_67"));
                e.setTransportGUID (tr.toString ());
                rq.getImportOrganizationWork ().add (e);
                break;
            default:
                logger.warning ("action not supported: " + action);
        }        
      
logger.info ("rq = " + rq);
    
        return getPort (orgPPAGuid).importOrganizationWorks (rq).getAck ();
        
    }
    
    public AckRequest.Ack importMunicipalServices (UUID orgPPAGuid, Map<String, Object> r) throws Fault {
        
        if (orgPPAGuid == null) throw new NullPointerException ();

        ImportMunicipalServicesRequest rq = of.createImportMunicipalServicesRequest ();
        
        final String action = r.get ("action").toString ();
        
        UUID tr = UUID.randomUUID ();
        
logger.info ("r = " + r);
        
        switch (action) {
            case "delete":
                final ImportMunicipalServicesRequest.DeleteMainMunicipalService d = (ImportMunicipalServicesRequest.DeleteMainMunicipalService) DB.to.javaBean (ImportMunicipalServicesRequest.DeleteMainMunicipalService.class, r);
                d.setTransportGUID (tr.toString ());
                rq.getDeleteMainMunicipalService ().add (d);
                break;
            case "create":
            case "update":
                final ImportMunicipalServicesRequest.ImportMainMunicipalService s = (ImportMunicipalServicesRequest.ImportMainMunicipalService) DB.to.javaBean (ImportMunicipalServicesRequest.ImportMainMunicipalService.class, r);
                if (s.getSortOrder () == null) s.setSortOrderNotDefined (Boolean.TRUE);
                s.setMunicipalServiceRef  (NsiTable.toDom ((String) r.get ("code_vc_nsi_3"), (UUID) r.get ("vc_nsi_3.guid")));
                s.setMunicipalResourceRef (NsiTable.toDom ((String) r.get ("code_vc_nsi_2"), (UUID) r.get ("vc_nsi_2.guid")));
                s.setTransportGUID (tr.toString ());
                rq.getImportMainMunicipalService ().add (s);
                break;
            default:
                logger.warning ("action not supported: " + action);
        }

logger.info ("rq = " + rq);

        return getPort (orgPPAGuid).importMunicipalServices (rq).getAck ();
        
    }    

    public AckRequest.Ack exportDataProviderNsiItem (UUID orgPPAGuid, UUID messageGUID, long registryNumber) throws Fault {
        final ExportDataProviderNsiItemRequest rq = new ExportDataProviderNsiItemRequest ();
        rq.setRegistryNumber (BigInteger.valueOf (registryNumber));
        return getPort (orgPPAGuid, messageGUID).exportDataProviderNsiItem (rq).getAck ();
    }
    
    public AckRequest.Ack importAdditionalServices (UUID orgPPAGuid, Map<String, Object> r) throws Fault {
        
        if (orgPPAGuid == null) throw new NullPointerException ();
        
        ImportAdditionalServicesRequest rq = of.createImportAdditionalServicesRequest ();
        
        final String action = r.get ("action").toString ();
        
        UUID tr = UUID.randomUUID ();

        switch (action) {
            case "delete":
                rq.getDeleteAdditionalServiceType ().add ((ImportAdditionalServicesRequest.DeleteAdditionalServiceType) DB.to.javaBean (ImportAdditionalServicesRequest.DeleteAdditionalServiceType.class, r));
                rq.getDeleteAdditionalServiceType ().get (0).setTransportGUID (tr.toString ());
                break;
            case "undelete":
                rq.getRecoverAdditionalServiceType ().add ((ImportAdditionalServicesRequest.RecoverAdditionalServiceType) DB.to.javaBean (ImportAdditionalServicesRequest.RecoverAdditionalServiceType.class, r));
                rq.getRecoverAdditionalServiceType ().get (0).setTransportGUID (tr.toString ());
                break;
            case "create":
            case "update":
                rq.getImportAdditionalServiceType ().add ((ImportAdditionalServicesRequest.ImportAdditionalServiceType) DB.to.javaBean (ImportAdditionalServicesRequest.ImportAdditionalServiceType.class, r));
                rq.getImportAdditionalServiceType ().get (0).setTransportGUID (tr.toString ());
                break;
            default:
                logger.warning ("action not supported: " + action);
        }        

        return getPort (orgPPAGuid).importAdditionalServices (rq).getAck ();
        
    }
    
}