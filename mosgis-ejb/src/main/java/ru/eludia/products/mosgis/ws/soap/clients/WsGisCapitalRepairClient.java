package ru.eludia.products.mosgis.ws.soap.clients;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import ru.eludia.products.mosgis.db.model.tables.BankAccountLog;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramHouseWorkLog;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramHouseWorksImport;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramLog;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWorkLog;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWorksImport;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramLog;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramHouseWorkLog;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramHouseWorksImport;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramLog;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.soap.tools.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair_service_async.CapitalRepairAsyncService;
import ru.gosuslugi.dom.schema.integration.capital_repair_service_async.CapitalRepairAsyncPort;
import ru.gosuslugi.dom.schema.integration.capital_repair_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.capital_repair.GetStateResult;
import ru.gosuslugi.dom.schema.integration.nsi.ObjectFactory;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisCapitalRepairClient {
    
    private static final ObjectFactory of = new ObjectFactory ();
    
    private static final Logger logger = Logger.getLogger (WsGisCapitalRepairClient.class.getName ());
    
    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/capital-repair/hcs-capital-repair-service-async.wsdl")
    private CapitalRepairAsyncService service;
    
    private CapitalRepairAsyncPort getPort (UUID orgPPAGuid, UUID messageGUID) {
        CapitalRepairAsyncPort port = service.getCapitalRepairAsyncPort ();
        VocSetting.setPort (port, "WS_GIS_CAPITAL_REPAIR");
        final Map <String, Object> requestContext = ((BindingProvider)port).getRequestContext ();
        requestContext.put (LoggingOutMessageHandler.FIELD_MESSAGE_GUID, messageGUID);
        requestContext.put (LoggingOutMessageHandler.FIELD_ORG_PPA_GUID, orgPPAGuid);
        return port;
    }
    
    public GetStateResult getState (UUID orgPPAGuid, UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort (orgPPAGuid, UUID.randomUUID ()).getState (getStateRequest);
    }
    
    public AckRequest.Ack importRegionalProgramProject (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importRegionalProgram (OverhaulRegionalProgramLog.toImportRegionalProgramRequest (r, true)).getAck ();
    }
    
    public AckRequest.Ack importRegionalProgram (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importRegionalProgram (OverhaulRegionalProgramLog.toImportRegionalProgramRequest (r, false)).getAck ();
    }
    
    public AckRequest.Ack importRegionalProgramWork (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importRegionalProgramWork(OverhaulRegionalProgramHouseWorksImport.toImportRegionalProgramWorkRequest (r)).getAck ();
    }
    
    public AckRequest.Ack annulRegionalProgramWork (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importRegionalProgramWork(OverhaulRegionalProgramHouseWorkLog.toAnnulRegionalProgramWorkRequest (r)).getAck ();
    }
    
    public AckRequest.Ack deleteRegionalProgramProject (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importRegionalProgram (OverhaulRegionalProgramLog.toDeleteRegionalProgramRequest (r)).getAck ();
    }
    
    public AckRequest.Ack annulRegionalProgram (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importRegionalProgram (OverhaulRegionalProgramLog.toAnnulRegionalProgramRequest (r)).getAck ();
    }
    
    //---SHORT PROGRAM---//
    
    public AckRequest.Ack importShortProgramProject (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importPlan (OverhaulShortProgramLog.toImportPlanRequest (r, true)).getAck ();
    }
    
    public AckRequest.Ack importShortProgram (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importPlan (OverhaulShortProgramLog.toImportPlanRequest (r, false)).getAck ();
    }
    
    public AckRequest.Ack importShortProgramWork (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importPlanWork (OverhaulShortProgramHouseWorksImport.toImportPlanWorkRequest (r)).getAck ();
    }
    
    public AckRequest.Ack annulShortProgramWork (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importPlanWork (OverhaulShortProgramHouseWorkLog.toAnnulPlanWorkRequest (r)).getAck ();
    }
    
    public AckRequest.Ack deleteShortProgramProject (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importPlan (OverhaulShortProgramLog.toDeletePlanRequest (r)).getAck ();
    }
    
    public AckRequest.Ack annulShortProgram (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importPlan (OverhaulShortProgramLog.toAnnulPlanRequest (r)).getAck ();
    }
    
    //---ADDRESS PROGRAM---//
    
    public AckRequest.Ack importAddressProgramProject (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importPlan (OverhaulAddressProgramLog.toImportPlanRequest (r, true)).getAck ();
    }
    
    public AckRequest.Ack importAddressProgram (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importPlan (OverhaulAddressProgramLog.toImportPlanRequest (r, false)).getAck ();
    }
    
    public AckRequest.Ack importAddressProgramWork (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importPlanWork (OverhaulAddressProgramHouseWorksImport.toImportPlanWorkRequest (r)).getAck ();
    }
    
    public AckRequest.Ack annulAddressProgramWork (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importPlanWork (OverhaulAddressProgramHouseWorkLog.toAnnulPlanWorkRequest (r)).getAck ();
    }
    
    public AckRequest.Ack deleteAddressProgramProject (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importPlan (OverhaulAddressProgramLog.toDeletePlanRequest (r)).getAck ();
    }
    
    public AckRequest.Ack annulAddressProgram (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importPlan (OverhaulAddressProgramLog.toAnnulPlanRequest (r)).getAck ();
    }

    //---REGIONAL OPERATOR BANK ACCOUNT ---//

    public AckRequest.Ack importRegionalOperatorAccount (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importRegionalOperatorAccounts (BankAccountLog.toImportAccountRegionalOperatorRequest (r)).getAck ();
    }
    
    public AckRequest.Ack terminateRegionalOperatorAccount (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importRegionalOperatorAccounts (BankAccountLog.toTerminateAccountRegionalOperatorRequest (r)).getAck ();
    }
}
