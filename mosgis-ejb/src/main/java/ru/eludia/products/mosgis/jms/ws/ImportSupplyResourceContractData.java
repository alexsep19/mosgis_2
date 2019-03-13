package ru.eludia.products.mosgis.jms.ws;

import java.sql.SQLException;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.jms.base.WsMDB;
import ru.eludia.products.mosgis.ws.soap.impl.base.Fault;
import ru.gosuslugi.dom.schema.integration.base.BaseAsyncResponseType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.GetStateResult;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inSoapImportSupplyResourceContractData")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ImportSupplyResourceContractData extends WsMDB {
    
    private final static JAXBContext jc;
    
    static {
        try {
            jc = JAXBContext.newInstance (GetStateResult.class);
        }
        catch (JAXBException ex) {
            throw new IllegalStateException (ex);
        }
    }

    @Override
    protected JAXBContext getJAXBContext() throws JAXBException {
        return jc;
    }

    @Override
    protected BaseAsyncResponseType handleRequest (DB db, Object request) throws Exception {
        
        try {
            return generateResponse (db, request);
        } 
        catch (Fault e) {
            GetStateResult result = new GetStateResult ();
            result.setErrorMessage (createErrorMessage (e));
            return result;
        }
        
    }
        
    static BaseAsyncResponseType generateResponse (DB db, Object request) throws Fault, SQLException {    
        
        return null;
                
    }

}
