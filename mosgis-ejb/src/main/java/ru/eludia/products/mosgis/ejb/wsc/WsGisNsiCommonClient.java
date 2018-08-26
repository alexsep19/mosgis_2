package ru.eludia.products.mosgis.ejb.wsc;

import java.math.BigInteger;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.xml.ws.WebServiceRef;
import ru.eludia.products.mosgis.db.model.voc.VocNsiListGroup;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiItemRequest;
import ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiListRequest;
import ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiPagingItemRequest;
import ru.gosuslugi.dom.schema.integration.nsi_common.GetStateResult;
import ru.gosuslugi.dom.schema.integration.nsi_common_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.nsi_common_service_async.NsiPortsTypeAsync;
import ru.gosuslugi.dom.schema.integration.nsi_common_service_async.NsiServiceAsync;
import ru.gosuslugi.dom.schema.integration.nsi_common.ObjectFactory;

@Stateless
public class WsGisNsiCommonClient {
    
    private static final ObjectFactory of = new ObjectFactory ();
    
    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/nsi-common/hcs-nsi-common-service-async.wsdl")
    private NsiServiceAsync service;
        
    private NsiPortsTypeAsync getPort () {
        ru.gosuslugi.dom.schema.integration.nsi_common_service_async.NsiPortsTypeAsync port = service.getNsiPortAsync();
        VocSetting.setPort (port, "WS_GIS_NSI_COMMON");
        return port;
    }

    public AckRequest.Ack exportNsiList (VocNsiListGroup.i listGroup) throws Fault {
        ExportNsiListRequest rq = of.createExportNsiListRequest ();
        rq.setListGroup (listGroup.getName ());
        return getPort ().exportNsiList (rq).getAck ();
    }

    public AckRequest.Ack exportNsiItem (VocNsiListGroup.i listGroup, int registryNumber) throws Fault {
        ExportNsiItemRequest rq = of.createExportNsiItemRequest ();
        rq.setListGroup (listGroup.getName ());
        rq.setRegistryNumber (BigInteger.valueOf (registryNumber));
        return getPort ().exportNsiItem (rq).getAck ();
    }
 
    public AckRequest.Ack exportNsiPagingItem (VocNsiListGroup.i listGroup, int registryNumber, int page) throws Fault {
        ExportNsiPagingItemRequest rq = of.createExportNsiPagingItemRequest ();
        rq.setListGroup (listGroup.getName ());
        rq.setRegistryNumber (BigInteger.valueOf (registryNumber));
        rq.setPage (page);
        return getPort ().exportNsiPagingItem (rq).getAck ();
    }

    public GetStateResult getState (UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort ().getState (getStateRequest);
    }
    
}