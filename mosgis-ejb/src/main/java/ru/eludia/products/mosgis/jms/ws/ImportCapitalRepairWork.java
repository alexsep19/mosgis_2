package ru.eludia.products.mosgis.jms.ws;

import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import ru.eludia.products.mosgis.jms.base.WsProxyMDB;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisNsiClient;
import ru.eludia.products.mosgis.ws.soap.impl.base.Fault;
import ru.gosuslugi.dom.schema.integration.base.AckRequest.Ack;
import ru.gosuslugi.dom.schema.integration.nsi.GetStateResult;
import ru.gosuslugi.dom.schema.integration.nsi.ImportCapitalRepairWorkRequest;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.importCapitalRepairWork"),
		@ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
public class ImportCapitalRepairWork extends WsProxyMDB {

	public final static JAXBContext JAXB_CONTEXT;
	
	static {
        try {
        	JAXB_CONTEXT = JAXBContext.newInstance (
        			ImportCapitalRepairWorkRequest.class, 
        			GetStateResult.class
            );
        }
        catch (JAXBException ex) {
            throw new IllegalStateException (ex);
        }
    }
	
	@EJB
	private WsGisNsiClient wsGisNsiClient;

	@Resource(mappedName = "mosgis.outImportCapitalRepairWork")
	private Queue outImportCapitalRepairWork;

	@Override
	protected Queue getPollQueue() {
		return outImportCapitalRepairWork;
	}

	@Override
	protected JAXBContext getJAXBContext() throws JAXBException {
		return JAXB_CONTEXT;
	}

	@Override
	protected Ack sendRequest(UUID orgPPAGuid, UUID messageGUID, Object request) throws Fault {

		ImportCapitalRepairWorkRequest importCapitalRepairWorkRequest = (ImportCapitalRepairWorkRequest) request;

		try {
			return wsGisNsiClient.importCapitalRepairWork(orgPPAGuid, messageGUID, importCapitalRepairWorkRequest);
		} catch (ru.gosuslugi.dom.schema.integration.nsi_service_async.Fault fault) {
			throw new Fault(fault.getFaultInfo());
		}
	}

}
