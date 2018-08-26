package ru.eludia.products.mosgis.ejb.wsc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import ru.gosuslugi.dom.schema.integration.bills.ObjectFactory;
import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ws.base.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.Attachment;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.bills.GetStateResult;
import ru.gosuslugi.dom.schema.integration.bills.ImportInsuranceProductRequest;
import ru.gosuslugi.dom.schema.integration.bills_service_async.BillsServiceAsync;
import ru.gosuslugi.dom.schema.integration.bills_service_async.Fault;

@Stateless
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
    
}
