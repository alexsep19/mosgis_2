package ru.eludia.products.mosgis.jms.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.eludia.products.mosgis.ws.soap.impl.base.Fault;
import ru.eludia.products.mosgis.ws.soap.impl.base.AbstactServiceAsync;
import ru.mos.gkh.gis.schema.integration.base.BaseAsyncResponseType;
import ru.mos.gkh.gis.schema.integration.base.ErrorMessageType;
import ru.mos.gkh.gis.schema.integration.base.ResultHeader;

/**
 *
 * @author Aleksei
 */
public abstract class WsMDB extends UUIDMDB<WsMessages>{
    
    private static final QName SOAP_SERVER_FAULT =
         new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Server", SOAPConstants.SOAP_ENV_PREFIX);
    
    protected abstract JAXBContext getJAXBContext() throws JAXBException;
    
    protected abstract BaseAsyncResponseType handleRequest(DB db, Object request) throws Exception ;
    
    @Override
    protected void handleRecord(DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        try {
            r.put(WsMessages.c.ID_STATUS.lc(), VocAsyncRequestState.i.IN_PROGRESS.getId());
            db.update(WsMessages.class, r);

            JAXBContext jaxbContext = getJAXBContext();

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            String request = r.get(WsMessages.c.REQUEST.lc()).toString();
            
            BaseAsyncResponseType result = handleRequest(db, unmarshaller.unmarshal(getSoapBodyNode(request)));
            
            result.setRequestState(VocAsyncRequestState.i.DONE.getId());
            result.setMessageGUID(uuid.toString());
            
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            Document document = getDocumentBuilder().newDocument();
            marshaller.marshal(result, document);

            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            SOAPBody soapBody = soapMessage.getSOAPBody();
            soapBody.addDocument(document);

            ResultHeader resultHeader = new ResultHeader();
            LocalDateTime responseDt = LocalDateTime.now();
            resultHeader.setDate(responseDt);
            resultHeader.setMessageGUID(r.get(WsMessages.c.UUID.lc()).toString());
            AbstactServiceAsync.addHeaderToResponse(soapMessage, resultHeader);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapMessage.writeTo(out);
            String responseStr = new String(out.toByteArray(), "UTF-8");

            r.put(WsMessages.c.RESPONSE_TIME.lc(), responseDt);
            r.put(WsMessages.c.RESPONSE.lc(), responseStr);
        } catch (Exception ex) {
            try {
                SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
                SOAPBody soapBody = soapMessage.getSOAPBody();
                soapBody.addFault(SOAP_SERVER_FAULT, ex.getClass().getName() + (StringUtils.isNotBlank(ex.getMessage()) ? ": " + ex.getMessage() : ""));
                
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                soapMessage.writeTo(out);
                String responseStr = new String(out.toByteArray(), "UTF-8");
            
                r.put(WsMessages.c.RESPONSE_TIME.lc(), LocalDateTime.now());
                r.put(WsMessages.c.RESPONSE.lc(), responseStr);
                r.put(WsMessages.c.HAS_ERROR.lc(), true);
                
            } catch (SOAPException | IOException e){
                logger.log (Level.SEVERE, "Cannot create SOAP fault", e);
            }
        } finally {
            r.put(WsMessages.c.ID_STATUS.lc(), VocAsyncRequestState.i.DONE.getId());
            db.update(WsMessages.class, r);
        }

    }

    private Node getSoapBodyNode(String message) throws SOAPException, ParserConfigurationException, SAXException, IOException {
        DocumentBuilder documentBuilder = getDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(message.getBytes()));

        StringBuilder stringBuilder = new StringBuilder();
        Element root = document.getDocumentElement();
        String rootPrefix = root.getPrefix();

        if (rootPrefix.isEmpty()) {
            stringBuilder.append("Body");
        } else {
            stringBuilder.append(rootPrefix).append(":Body");
        }

        NodeList nodeList = root.getElementsByTagName(stringBuilder.toString());
        if (nodeList == null || nodeList.item(0) == null) {
            throw new SOAPException("No Body tag found in document");
        }

        Node messageNode = nodeList.item(0).getFirstChild();
        while (messageNode != null && !(messageNode instanceof Element)) {
            messageNode = messageNode.getNextSibling();
        }

        if (messageNode == null) {
            throw new SOAPException("Missing message tag");
        }

        return messageNode;
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder;
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
