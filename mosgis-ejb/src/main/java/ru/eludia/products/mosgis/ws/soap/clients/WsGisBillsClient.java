package ru.eludia.products.mosgis.ws.soap.clients;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import ru.gosuslugi.dom.schema.integration.bills.ObjectFactory;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.SettlementDocLog;
import ru.eludia.products.mosgis.db.model.tables.PaymentDocumentLog;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.soap.tools.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.Attachment;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.bills.GetStateResult;
import ru.gosuslugi.dom.schema.integration.bills.ImportInsuranceProductRequest;
import ru.gosuslugi.dom.schema.integration.bills_service_async.BillsServiceAsync;
import ru.gosuslugi.dom.schema.integration.bills_service_async.Fault;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsGisBillsClient {
    
    private static final ObjectFactory of = new ObjectFactory ();
    
    private static final Logger logger = Logger.getLogger (WsGisBillsClient.class.getName ());
    
    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/bills/hcs-bills-service-async.wsdl")
    private BillsServiceAsync service;

    private ru.gosuslugi.dom.schema.integration.bills_service_async.BillsPortsTypeAsync getPort (UUID orgPPAGuid) {
        ru.gosuslugi.dom.schema.integration.bills_service_async.BillsPortsTypeAsync port = service.getBillsPortAsync ();
        VocSetting.setPort (port, "WS_GIS_BILLS");
        ((BindingProvider)port).getRequestContext ().put (LoggingOutMessageHandler.FIELD_ORG_PPA_GUID, orgPPAGuid);
        return port;
    }    

    private ru.gosuslugi.dom.schema.integration.bills_service_async.BillsPortsTypeAsync getPort(UUID orgPPAGuid, UUID messageGUID) {
	ru.gosuslugi.dom.schema.integration.bills_service_async.BillsPortsTypeAsync port = service.getBillsPortAsync();
	VocSetting.setPort(port, "WS_GIS_BILLS");
	final Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
	requestContext.put(LoggingOutMessageHandler.FIELD_MESSAGE_GUID, messageGUID);
	requestContext.put(LoggingOutMessageHandler.FIELD_ORG_PPA_GUID, orgPPAGuid);
	return port;
    }

    public GetStateResult getState (UUID orgPPAGuid, UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort (orgPPAGuid).getState (getStateRequest);
    }
    
    public AckRequest.Ack setInsuranceProduct (UUID orgPPAGuid, Map<String, Object> r) throws Fault {
        
        final ImportInsuranceProductRequest.InsuranceProduct p = of.createImportInsuranceProductRequestInsuranceProduct ();
        p.setTransportGUID (UUID.randomUUID ().toString ());

        if ("delete".equals (r.get ("action"))) {

            final ImportInsuranceProductRequest.InsuranceProduct.Remove rm = of.createImportInsuranceProductRequestInsuranceProductRemove ();
            rm.setCloseDate (DB.to.XMLGregorianCalendar (java.sql.Timestamp.from (Instant.now ())));
            rm.setInsuranceProductGUID (r.get ("insuranceproductguid").toString ());            
            p.setRemove (rm);

        }
        else {
            
            final Attachment a = new Attachment ();
            a.setAttachmentGUID (r.get ("attachmentguid").toString ());

            final AttachmentType at = new AttachmentType ();
            at.setAttachment (a);
            at.setName        (r.get ("name").toString ());
            at.setDescription (r.get ("description").toString ());

            final ImportInsuranceProductRequest.InsuranceProduct.CreateOrUpdate cu = of.createImportInsuranceProductRequestInsuranceProductCreateOrUpdate ();
            cu.setDescription (at);
            cu.setInsuranceOrg         (r.get ("insuranceorg").toString ());
            
            final Object uuid = r.get ("insuranceproductguid");            
            if (uuid != null) cu.setInsuranceProductGUID (uuid.toString ());

            p.setCreateOrUpdate (cu);
            
        }                

        final ImportInsuranceProductRequest rq = of.createImportInsuranceProductRequest ();
        rq.getInsuranceProduct ().add (p);

        return getPort (orgPPAGuid).importInsuranceProduct (rq).getAck ();
        
    }

    public AckRequest.Ack importSettlementDocRSO(UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
	return getPort(orgPPAGuid, messageGUID).importRSOSettlements(SettlementDocLog.toImportRSOSettlementsRequest(r)).getAck();
    }

    public AckRequest.Ack importSettlementDocUO(UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
	return getPort(orgPPAGuid, messageGUID).importIKUSettlements(SettlementDocLog.toImportIKUSettlementsRequest(r)).getAck();
    }

    public AckRequest.Ack annulSettlementDocRSO(UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
	return getPort(orgPPAGuid, messageGUID).importRSOSettlements(SettlementDocLog.toAnnulRSOSettlementsRequest(r)).getAck();
    }

    public AckRequest.Ack annulSettlementDocUO(UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
	return getPort(orgPPAGuid, messageGUID).importIKUSettlements(SettlementDocLog.toAnnulIKUSettlementsRequest(r)).getAck();
    }
    
    public AckRequest.Ack importPaymentDocumentData (UUID orgPPAGuid, UUID messageGUID, Map<String, Object> r) throws Fault {
        return getPort (orgPPAGuid, messageGUID).importPaymentDocumentData (PaymentDocumentLog.toImportPaymentDocumentRequest (r)).getAck ();
    } 
    
}
