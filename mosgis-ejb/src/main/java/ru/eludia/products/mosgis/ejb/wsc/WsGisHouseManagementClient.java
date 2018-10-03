package ru.eludia.products.mosgis.ejb.wsc;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractFile;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.base.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChAsyncRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChRequestCriteriaType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportHouseRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ExportStatusCAChRequest;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.ImportContractRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.HouseManagementPortsTypeAsync;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.HouseManagementServiceAsync;

@Stateless
public class WsGisHouseManagementClient {

    private static final ObjectFactory of = new ObjectFactory ();
    private static final Logger logger = Logger.getLogger (WsGisHouseManagementClient.class.getName ());

    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/house-management/hcs-house-management-service-async.wsdl")
    private HouseManagementServiceAsync service;

    private HouseManagementPortsTypeAsync getPort () {
        HouseManagementPortsTypeAsync port = service.getHouseManagementPortAsync();
        VocSetting.setPort (port, "WS_GIS_HOUSE_MANAGEMENT");
        return port;
    }

    private HouseManagementPortsTypeAsync getPort (UUID orgPPAGuid, UUID messageGUID) {
        HouseManagementPortsTypeAsync port = service.getHouseManagementPortAsync();
        VocSetting.setPort (port, "WS_GIS_HOUSE_MANAGEMENT");
        final Map<String, Object> requestContext = ((BindingProvider)port).getRequestContext ();
        requestContext.put (LoggingOutMessageHandler.FIELD_MESSAGE_GUID, messageGUID);
        requestContext.put (LoggingOutMessageHandler.FIELD_ORG_PPA_GUID, orgPPAGuid);
        return port;
    }

    public GetStateResult getState (UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort ().getState(getStateRequest);
    }    

    public AckRequest.Ack exportHouseData (String fiasHouseGuid) throws Fault {
        
        if (fiasHouseGuid == null) throw new IllegalArgumentException ("Null FIASHouseGUID passed");
        
        ExportHouseRequest request = of.createExportHouseRequest();
        request.setFIASHouseGuid(fiasHouseGuid);
        
        return getPort ().exportHouseData(request).getAck();
        
    }

    public AckRequest.Ack rolloverContractData (UUID orgPPAGuid, UUID messageGUID,  Map<String, Object> r) throws Fault {

        final ImportContractRequest.Contract.RollOverContract rc = (ImportContractRequest.Contract.RollOverContract) DB.to.javaBean (ImportContractRequest.Contract.RollOverContract.class, r);
        rc.setLicenseRequest (true);
        rc.setContractVersionGUID (r.get ("ctr.contractversionguid").toString ());        
        rc.setRollOver (true);

        ImportContractRequest importContractRequest = of.createImportContractRequest ();
        final ImportContractRequest.Contract c = of.createImportContractRequestContract ();
        c.setRollOverContract (rc);
        c.setTransportGUID (UUID.randomUUID ().toString ());
        
        importContractRequest.getContract ().add (c);
        
        return getPort (orgPPAGuid, messageGUID).importContractData (importContractRequest).getAck ();        
        
    }
    
    public AckRequest.Ack annulContractData (UUID orgPPAGuid, UUID messageGUID,  Map<String, Object> r) throws Fault {

        final ImportContractRequest.Contract.AnnulmentContract ac = (ImportContractRequest.Contract.AnnulmentContract) DB.to.javaBean (ImportContractRequest.Contract.AnnulmentContract.class, r);
        ac.setLicenseRequest (true);
        ac.setContractVersionGUID (r.get ("ctr.contractversionguid").toString ());
        
        ImportContractRequest importContractRequest = of.createImportContractRequest ();
        final ImportContractRequest.Contract c = of.createImportContractRequestContract ();
        c.setAnnulmentContract (ac);
        c.setTransportGUID (UUID.randomUUID ().toString ());
        
        importContractRequest.getContract ().add (c);
        
        return getPort (orgPPAGuid, messageGUID).importContractData (importContractRequest).getAck ();        
        
    }

    public AckRequest.Ack terminateContractData (UUID orgPPAGuid, UUID messageGUID,  Map<String, Object> r) throws Fault {
        
        final ImportContractRequest.Contract.TerminateContract tc = (ImportContractRequest.Contract.TerminateContract) DB.to.javaBean (ImportContractRequest.Contract.TerminateContract.class, r);
        
        tc.setLicenseRequest (true);
        tc.setContractVersionGUID (r.get ("ctr.contractversionguid").toString ());
        
        List<AttachmentType> terminateAttachment = tc.getTerminateAttachment ();
        
        String idTerminationType = String.valueOf (VocContractDocType.i.TERMINATION_ATTACHMENT.getId ());        
        for (Map<String, Object> file: (Collection<Map<String, Object>>) r.get ("files")) {
            if (!idTerminationType.equals (file.get ("id_type").toString ())) continue;
            terminateAttachment.add (ContractFile.toAttachmentType (file));
        }
        
        tc.setReasonRef (NsiTable.toDom (r.get ("code_vc_nsi_54").toString (), (UUID) r.get ("vc_nsi_54.guid")));
        
        ImportContractRequest importContractRequest = of.createImportContractRequest ();
        final ImportContractRequest.Contract c = of.createImportContractRequestContract ();
        c.setTerminateContract (tc);
        c.setTransportGUID (UUID.randomUUID ().toString ());
        
        importContractRequest.getContract ().add (c);

        return getPort (orgPPAGuid, messageGUID).importContractData (importContractRequest).getAck ();
        
    }

    public AckRequest.Ack editContractData (UUID orgPPAGuid, UUID messageGUID,  Map<String, Object> r) throws Fault {
        
        final ImportContractRequest.Contract.EditContract ec = (ImportContractRequest.Contract.EditContract) DB.to.javaBean (ImportContractRequest.Contract.EditContract.class, r);
        
        ec.setLicenseRequest (true);
        Contract.fillContract (ec, r);
        ec.setContractVersionGUID (r.get ("ctr.contractversionguid").toString ());

        for (Map<String, Object> file: (Collection<Map<String, Object>>) r.get ("files"))   ContractFile.add   (ec, file);
        for (Map<String, Object> o:    (Collection<Map<String, Object>>) r.get ("objects")) ContractObject.add (ec, o);
        
        ImportContractRequest importContractRequest = of.createImportContractRequest ();
        final ImportContractRequest.Contract c = of.createImportContractRequestContract ();
        c.setEditContract (ec);
        c.setTransportGUID (UUID.randomUUID ().toString ());
        
        importContractRequest.getContract ().add (c);

        return getPort (orgPPAGuid, messageGUID).importContractData (importContractRequest).getAck ();
        
    }
    
    public AckRequest.Ack placeContractData (UUID orgPPAGuid, UUID messageGUID,  Map<String, Object> r) throws Fault {
        
        final ImportContractRequest.Contract.PlacingContract pc = (ImportContractRequest.Contract.PlacingContract) DB.to.javaBean (ImportContractRequest.Contract.PlacingContract.class, r);
        pc.setLicenseRequest (true);
        Contract.fillContract (pc, r);
        
        for (Map<String, Object> file: (Collection<Map<String, Object>>) r.get ("files"))   ContractFile.add   (pc, file);
        for (Map<String, Object> o:    (Collection<Map<String, Object>>) r.get ("objects")) ContractObject.add (pc, o);

        ImportContractRequest importContractRequest = of.createImportContractRequest ();
        
        final ImportContractRequest.Contract c = of.createImportContractRequestContract ();
        c.setPlacingContract (pc);        
        c.setTransportGUID (UUID.randomUUID ().toString ());
        
        importContractRequest.getContract ().add (c);

        return getPort (orgPPAGuid, messageGUID).importContractData (importContractRequest).getAck ();
        
    }
    
    public AckRequest.Ack approveContractData (UUID orgPPAGuid, UUID messageGUID, UUID contractVersionGUID) throws Fault {
        
        final ImportContractRequest.Contract.ApprovalContract ac = of.createImportContractRequestContractApprovalContract ();
        ac.setApproval (true);
        ac.setContractVersionGUID (contractVersionGUID.toString ());
        
        ImportContractRequest importContractRequest = of.createImportContractRequest ();
        
        final ImportContractRequest.Contract c = of.createImportContractRequestContract ();
        c.setApprovalContract (ac);
        c.setTransportGUID (UUID.randomUUID ().toString ());
        
        importContractRequest.getContract ().add (c);

        return getPort (orgPPAGuid, messageGUID).importContractData (importContractRequest).getAck ();
        
    }
    
    public AckRequest.Ack exportContractStatus (UUID orgPPAGuid, UUID messageGUID, List<UUID> ids) throws Fault {

        final ExportStatusCAChRequest r = of.createExportStatusCAChRequest ();

        List<ExportStatusCAChRequest.Criteria> criteria = r.getCriteria ();

        for (UUID uuid: ids) {
            final ExportStatusCAChRequest.Criteria c = of.createExportStatusCAChRequestCriteria ();            
            c.setContractGUID (uuid.toString ());            
            criteria.add (c);            
        }

        return getPort (orgPPAGuid, messageGUID).exportStatusCAChData (r).getAck ();

    }   

    public AckRequest.Ack exportContractData (UUID orgPPAGuid, UUID messageGUID, List<UUID> ids) throws Fault {

        final ExportCAChAsyncRequest r = of.createExportCAChAsyncRequest ();
        
        List<ExportCAChRequestCriteriaType> criteria = r.getCriteria ();

        for (UUID uuid: ids) {
            ExportCAChRequestCriteriaType c = of.createExportCAChRequestCriteriaType ();
            c.setContractGUID (uuid.toString ());
            criteria.add (c);
        }

        return getPort (orgPPAGuid, messageGUID).exportCAChData (r).getAck ();

    }

    public GetStateResult getState (UUID orgPPAGuid, UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort (orgPPAGuid, UUID.randomUUID ()).getState (getStateRequest);
    }    

}