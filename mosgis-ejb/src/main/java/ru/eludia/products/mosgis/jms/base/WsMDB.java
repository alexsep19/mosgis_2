package ru.eludia.products.mosgis.jms.base;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Document;

import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.ws.soap.impl.base.Fault;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.base.BaseAsyncResponseType;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.base.ResultHeader;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.GetStateResult;

public abstract class WsMDB extends UUIDMDB<WsMessages>{
    
    protected abstract JAXBContext getJAXBContext() throws JAXBException;
        
    protected abstract BaseAsyncResponseType generateResponse (DB db, Map<String, Object> r, Object request) throws Exception;    
    
    protected BaseAsyncResponseType handleRequest (DB db, Map<String, Object> r, Object request) throws Exception {
        
        try {            
            return generateResponse (db, r, request);            
        } 
        catch (Fault e) {
            GetStateResult result = new GetStateResult();
            result.setErrorMessage(createErrorMessage(e));
            return result;
        }
        
    }
    
    @Override
    protected void handleRecord(DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        try {
            r.put(WsMessages.c.ID_STATUS.lc(), VocAsyncRequestState.i.IN_PROGRESS.getId());
            db.update(WsMessages.class, r);

            JAXBContext jaxbContext = getJAXBContext();

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            String request = r.get(WsMessages.c.REQUEST.lc()).toString();
            
            BaseAsyncResponseType result = handleRequest (db, r, unmarshaller.unmarshal(SOAPTools.getSoapBodyNode(request)));
            
            result.setRequestState(VocAsyncRequestState.i.DONE.getId());
            result.setMessageGUID(uuid.toString());
            
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            Document document = SOAPTools.getDocumentBuilder().newDocument();
            marshaller.marshal(result, document);

            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            SOAPBody soapBody = soapMessage.getSOAPBody();
            soapBody.addDocument(document);

            ResultHeader resultHeader = new ResultHeader();
            
            final Timestamp now = new Timestamp (System.currentTimeMillis ());
            resultHeader.setDate (DB.to.XMLGregorianCalendar (now));
            resultHeader.setMessageGUID(r.get(WsMessages.c.UUID.lc()).toString());
            SOAPTools.addHeaderToResponse(soapMessage, resultHeader);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapMessage.writeTo(out);
            String responseStr = new String(out.toByteArray(), "UTF-8");

            r.put(WsMessages.c.RESPONSE_TIME.lc(), now);
            r.put(WsMessages.c.RESPONSE.lc(), responseStr);
            r.put(WsMessages.c.ID_STATUS.lc(), VocAsyncRequestState.i.DONE.getId());
            
            db.update(WsMessages.class, r);
            
        } catch (Exception ex) {

	    logger.log(Level.SEVERE, "Cannot generate SOAP response", ex);

	    WsMessages.registerException(db, uuid, ex);
        }

    }

    protected ErrorMessageType createErrorMessage(Fault e) {
        ErrorMessageType errorMessage = new ErrorMessageType();
        errorMessage.setErrorCode(e.getFaultCode());
        errorMessage.setDescription(e.getFaultMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        sw.getBuffer().toString();
        errorMessage.setStackTrace(sw.getBuffer().toString());
        return errorMessage;
    }        
}
