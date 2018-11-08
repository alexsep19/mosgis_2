package ru.eludia.products.mosgis.ejb.wsc;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceRef;
import org.tempuri.WS;
import org.tempuri.IWS;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import org.datacontract.schemas._2004._07.dmwsnewlife.ArrayOfObjectPropertyModelService;
import org.datacontract.schemas._2004._07.dmwsnewlife.ArrayOfOwnObjectModelService;
import org.datacontract.schemas._2004._07.dmwsnewlife.ArrayOfOwnObjectService;
import org.datacontract.schemas._2004._07.dmwsnewlife.OwnObjectService;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WsRdClient {

    @WebServiceRef(wsdlLocation = "META-INF/wsdl/rd.wsdl")
    private WS service;

    private IWS getPort () {
        IWS port = service.getBasicHttpBindingIWS ();
        VocSetting.setPort (port, "WS_RD");
        return port;
    }

    public ArrayOfObjectPropertyModelService getModelProperties (int modelId) {
        IWS port = getPort ();
        return port.getModelProperties (modelId);
    }

    public ArrayOfOwnObjectModelService getAllModelsTreeByRootId (int modelId) {
        IWS port = getPort ();
        return port.getAllModelsTreeByRootId (modelId);
    }

    public ArrayOfOwnObjectService getObjectsByModel (int modelId, boolean withProp) {
        IWS port = getPort ();
        return port.getObjectsByModel (modelId, withProp, null, null, null);
    }
    
    public OwnObjectService getObjectByObjectID (int objID, int modelID) {
        IWS port = getPort ();
        return port.getObjectByObjectID (objID, modelID, Boolean.TRUE);
    }

}