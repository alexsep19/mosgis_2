package ru.eludia.products.mosgis.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jms.Queue;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import ru.eludia.base.DB;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.mos.gkh.gis.schema.integration.base.AckRequest;
import ru.mos.gkh.gis.schema.integration.base.GetStateRequest;
import ru.mos.gkh.gis.schema.integration.base.RequestHeader;
import ru.mos.gkh.gis.schema.integration.organizations_registry_common.ExportOrgRegistryRequest;
import ru.mos.gkh.gis.schema.integration.organizations_registry_common.GetStateResult;
import ru.mos.gkh.gis.schema.integration.organizations_registry_common_service_async.Fault;

/**
 *
 * @author Aleksei
 */
@HandlerChain (file="handler-chain.xml")
@WebService(
        serviceName = "RegOrgServiceAsync", 
        portName = "RegOrgAsyncPort", 
        endpointInterface = "ru.mos.gkh.gis.schema.integration.organizations_registry_common_service_async.RegOrgPortsTypeAsync", 
        targetNamespace = "http://gis.gkh.mos.ru/schema/integration/organizations-registry-common-service-async/", 
        wsdlLocation = "META-INF/ws/wsdl/organizations-registry-common/hcs-organizations-registry-common-service-async.wsdl")
@Stateless
public class RegOrgServiceCommonAsync {

    @Resource
    private WebServiceContext wsContext;
    
    @Resource (mappedName = "mosgis.exportOrgRegistry")
    private Queue exportOrgRegistryQueue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    public GetStateResult getState(GetStateRequest getStateRequest) throws Fault {
        
        MessageContext msgContext = wsContext.getMessageContext();
        
        GetStateResult result = new GetStateResult();
        try (DB db = ModelHolder.getModel().getDb()) {
            
            Map<String, Object> wsMsg = db.getMap(WsMessages.class, getStateRequest.getMessageGUID());
            
            if (wsMsg == null || wsMsg.isEmpty())
                throw new Exception("Не найдено сообщение с идентификатором " + getStateRequest.getMessageGUID());
            
            msgContext.put("msgId", wsMsg.get(WsMessages.c.UUID_MESSAGE.lc()));
            
            if (VocAsyncRequestState.i.ACCEPTED.equals(wsMsg.get(WsMessages.c.ID_STATUS.lc())))
                throw new Exception("OK");
            else {
                result.setMessageGUID(wsMsg.get(WsMessages.c.UUID.lc()).toString());
                result.setRequestState(Byte.valueOf(wsMsg.get(WsMessages.c.ID_STATUS.lc()).toString()));
            }
            
            return result;
            
        } catch (Exception ex) {
            ru.mos.gkh.gis.schema.integration.base.Fault fault = new ru.mos.gkh.gis.schema.integration.base.Fault();
            fault.setErrorCode("EXP000000");
            fault.setErrorMessage(ex.getMessage());
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            ex.printStackTrace(pw);
            sw.getBuffer().toString();
            fault.setStackTrace(sw.getBuffer().toString());
            
            throw new Fault(ex.getMessage(), fault);
        }
    }
    
    public AckRequest exportOrgRegistry(ExportOrgRegistryRequest exportOrgRegistryRequest) throws Fault {
        MessageContext msgContext = wsContext.getMessageContext();
        AckRequest.Ack ack = (AckRequest.Ack) msgContext.get("ack");
        AckRequest result = new AckRequest();
        result.setAck(ack);
        
        if (msgContext.get("isNew") != null && ((Boolean) msgContext.get("isNew")))
            UUIDPublisher.publish(exportOrgRegistryQueue, msgContext.get("msgId").toString());
        
        return result;
    }
    
}
