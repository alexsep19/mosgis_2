package ru.eludia.products.mosgis.jms.gis.poll.base;

import static ru.eludia.base.DB.HASH;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Document;

import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.base.BaseAsyncResponseType;
import ru.gosuslugi.dom.schema.integration.base.ResultHeader;

public abstract class GisPollWsProxyMDB<T extends BaseAsyncResponseType> extends GisPollMDB {
    
	protected abstract JAXBContext getJAXBContext() throws JAXBException;
	
	protected abstract T getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException;
	
	protected abstract void saveResponse (DB db, T response, Map<String, Object> r) throws SQLException, GisPollException;
	
	protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")
        		.toOne(WsMessages.class, "AS ws", "*").on ("root.uuid = ws.uuid");
    }
	
	@Override
	protected void handleOutSoapRecord(DB db, UUID uuid, Map<String, Object> r) throws SQLException {

		UUID orgPPAGuid = (UUID) r.get("orgppaguid");
		
		UUID wsMessageGuid = (UUID) r.get("ws." + WsMessages.c.UUID_MESSAGE.lc());

		T response = null;
		Map<String, Object> outSoap = null;
		try {

			response = getState(orgPPAGuid, r);
			
			outSoap = db.getMap(db.getModel().get(OutSoap.class, uuid, "rp", "ts_rp"));

			saveResponse(db, response, r);
			
			db.update(OutSoap.class, HASH(
					"uuid",      uuid,
					"id_status", VocAsyncRequestState.i.DONE.getId()));
				
			createWsMessageResponse(db, response, outSoap.get("ts_rp"), wsMessageGuid, uuid);

		} catch (GisPollRetryException ex) {
		} catch (GisPollException ex) {
			ex.register(db, uuid, r);
			if (response != null)
				createWsMessageResponse(db, response, outSoap.get("ts_rp"), wsMessageGuid, uuid);
			else
				WsMessages.registerException(db, uuid, ex);
		}

	}
	
	private void createWsMessageResponse(DB db, BaseAsyncResponseType response, Object responseDateTime, UUID headerMessageGuid, UUID uuid) throws SQLException {
		try {
			String responseXml = marshalResponse(
					response, 
					uuid.toString(), 
					headerMessageGuid.toString(),
					TypeConverter.timestamp(responseDateTime));
			
			db.update(WsMessages.class, HASH(
					WsMessages.c.UUID,          uuid,
					WsMessages.c.RESPONSE_TIME, responseDateTime, 
					WsMessages.c.RESPONSE,      responseXml, 
					WsMessages.c.HAS_ERROR,     true, 
					WsMessages.c.ID_STATUS,     VocAsyncRequestState.i.DONE.getId()));
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot modify SOAP response", ex);
			WsMessages.registerException(db, uuid, ex);
		}
	}
    
    private String marshalResponse(BaseAsyncResponseType response, String responseMessageGuid, String headerMessageGuid, Timestamp headerDate)
			throws SOAPException, IOException, ParserConfigurationException, JAXBException {
		
		response.setId(null);
		response.setSignature(null);
		response.setMessageGUID(responseMessageGuid);
		
		Marshaller marshaller = getJAXBContext().createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		Document document = SOAPTools.getDocumentBuilder().newDocument();
		marshaller.marshal(response, document);

		SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
		SOAPBody soapBody = soapMessage.getSOAPBody();
		soapBody.addDocument(document);

		ResultHeader resultHeader = new ResultHeader();

		resultHeader.setDate(TypeConverter.XMLGregorianCalendar(headerDate));
		resultHeader.setMessageGUID(headerMessageGuid);
		SOAPTools.addHeaderToResponse(soapMessage, resultHeader);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		soapMessage.writeTo(out);
		return new String(out.toByteArray(), "UTF-8");
	}
    
}
