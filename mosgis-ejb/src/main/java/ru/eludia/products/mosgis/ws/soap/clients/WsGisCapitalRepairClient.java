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
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWorksImport;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramLog;
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
    
}
