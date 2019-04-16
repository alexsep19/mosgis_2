package ru.eludia.products.mosgis.ws.soap.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.eludia.base.DB;
import ru.gosuslugi.dom.schema.integration.base.RequestHeader;
import ru.gosuslugi.dom.schema.integration.base.HeaderType;
import ru.gosuslugi.dom.schema.integration.base.ISRequestHeader;
import ru.gosuslugi.dom.schema.integration.base.ResultHeader;

public abstract class SOAPTools {
                
    private static JAXBContext jc;
        
    static {
        
        try {
            jc = JAXBContext.newInstance (
                    ISRequestHeader.class, 
                    RequestHeader.class, 
                    ResultHeader.class, 
                    ru.gosuslugi.dom.schema.integration.organizations_registry_common.GetStateResult.class
            );
        }
        catch (JAXBException ex) {
            throw new IllegalStateException ("Cannot create JAXBContext", ex);
        }

    }
    
    public static final Schema loadSchema (String path) throws URISyntaxException, SAXException {
        System.setProperty ("jdk.xml.maxOccurLimit", "100000");
        SchemaFactory schemaFactory = SchemaFactory.newInstance (XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL resource =  (ru.eludia.products.mosgis.ws.soap.impl.base.Error.EXP001000).getClass ().getClassLoader ().getResource ("META-INF/wsdl/" + path);
        File file = new File (resource.toURI ());
        return schemaFactory.newSchema (file);
    }
    
    public static final void setRequestHeader (SOAPMessage msg, HeaderType rh) {

        try {
            
            SOAPHeader h = msg.getSOAPHeader ();

            jc.createMarshaller ().marshal (rh, h);
            
            msg.saveChanges ();            

        }
        catch (Exception ex) {
            throw new IllegalStateException ("Cannot parse SOAP header", ex);
        }

    }

    public static final <T> T getHeader (SOAPMessage msg, Class<T> t) {
        try {            
            
            Iterator it = msg.getSOAPHeader ().examineAllHeaderElements ();

            while (it.hasNext ()) {

                try {
                    
                    Object o = jc.createUnmarshaller ().unmarshal ((SOAPHeaderElement) it.next ());

                    if (t.isAssignableFrom(o.getClass ())) return (T)o;
                
                }
                catch (Exception e) {
                    // ignore
                }                
                
            }
            
            return null;

        }
        catch (Exception ex) {
            throw new IllegalStateException ("Cannot parse SOAP header", ex);
        }
        
    }
    
    public static final <T> void addHeaderToResponse(SOAPMessage msg, T header) {
        try {
            SOAPHeader soapHeader = msg.getSOAPPart().getEnvelope().getHeader();
            if (soapHeader == null)
                soapHeader = msg.getSOAPPart().getEnvelope().addHeader();
            jc.createMarshaller().marshal(header, soapHeader);
        } catch (JAXBException | SOAPException ex) {
            throw new IllegalStateException("Cannot create SOAP header", ex);
        }
    }
    
    public static final ResultHeader getResultHeader (SOAPMessage msg) {
        
        return (ResultHeader) getHeader (msg, ResultHeader.class);

    }

    public static final RequestHeader getRequestHeader (SOAPMessage msg) {
        
        return (RequestHeader) getHeader (msg, RequestHeader.class);

    }

    public static Object fromXML (String xml) {
        
        try {
            
            Unmarshaller u = jc.createUnmarshaller ();
            
            try (StringReader sr = new StringReader (xml)) {
                
                return u.unmarshal (sr);
                
            }
            
        }
        catch (JAXBException ex) {
            throw new IllegalArgumentException ("Cannot parse " + xml, ex);
        }
        
    }
    
    public static String toXML (Object dom) {
        
        if (dom == null) return "<null />";
        
        StringWriter sw = new StringWriter ();
        
        try {
            Marshaller m = jc.createMarshaller ();
            m.marshal (dom, sw);
        }
        catch (JAXBException ex) {
            throw new IllegalArgumentException ("Cannot serialize " + dom, ex);
        }

        return sw.toString ();
                    
    }    
    
    public static Object fromJSON (String json) {
        
        try {
            
            Unmarshaller u = jc.createUnmarshaller ();
            u.setProperty ("eclipselink.media-type", "application/json");
            
            try (StringReader sr = new StringReader (json)) {
                
                return u.unmarshal (sr);
                
            }
            
        }
        catch (JAXBException ex) {
            throw new IllegalArgumentException ("Cannot parse " + json, ex);
        }
        
    }
    
    public static final String toJSON (Object dom) {
        
        if (dom == null) return "null";
        
        StringWriter sw = new StringWriter ();
        
        try {
            Marshaller m = jc.createMarshaller ();
            m.setProperty ("eclipselink.media-type", "application/json");
            m.marshal (dom, sw);
        }
        catch (JAXBException ex) {
            throw new IllegalArgumentException ("Cannot serialize " + dom, ex);
        }

        return sw.toString ();
                    
    }

    public static XMLGregorianCalendar xmlNow () {
        return DB.to.XMLGregorianCalendar (new Timestamp (System.currentTimeMillis ()));
    }
    
	public static Node getSoapBodyNode(String message) throws ParserConfigurationException,
			UnsupportedEncodingException, SAXException, IOException, SOAPException {
		
		DocumentBuilder documentBuilder = getDocumentBuilder();
		Document document = documentBuilder.parse(new ByteArrayInputStream(message.getBytes("UTF-8")));

		StringBuilder stringBuilder = new StringBuilder();
		Element root = document.getDocumentElement();

		String rootPrefix = root.getPrefix();

		if (rootPrefix.isEmpty()) {
			stringBuilder.append("Body");
		} else {
			stringBuilder.append(rootPrefix).append(":Body");
		}

		final String tagName = stringBuilder.toString();

		NodeList nodeList = root.getElementsByTagName(tagName);

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
	
	public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder;
	}
    
}