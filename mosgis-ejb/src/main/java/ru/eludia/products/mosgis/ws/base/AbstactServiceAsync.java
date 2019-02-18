package ru.eludia.products.mosgis.ws.base;

import javax.xml.ws.WebServiceContext;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Header;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.UUID;
import javax.ejb.EJB;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.RequestHeader;
import ru.gosuslugi.dom.schema.integration.base.HeaderType;
import ru.gosuslugi.dom.schema.integration.base.ISRequestHeader;
import ru.gosuslugi.dom.schema.integration.base.ResultHeader;

public abstract class AbstactServiceAsync {
        
    protected ru.gosuslugi.dom.schema.integration.base.ObjectFactory of = new ru.gosuslugi.dom.schema.integration.base.ObjectFactory ();
        
    protected abstract WebServiceContext getContext ();

    private static JAXBContext jc;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    static {
        
        try {
            jc = JAXBContext.newInstance (
                    ISRequestHeader.class, 
                    RequestHeader.class, 
                    ResultHeader.class, 
                    ru.gosuslugi.dom.schema.integration.organizations_registry_common.GetStateResult.class,
                    ru.mos.gkh.gis.schema.integration.base.ISRequestHeader.class,
                    ru.mos.gkh.gis.schema.integration.base.RequestHeader.class,
                    ru.mos.gkh.gis.schema.integration.base.ResultHeader.class
            );
        }
        catch (JAXBException ex) {
            throw new IllegalStateException ("Cannot create JAXBContext", ex);
        }

    }
    
    public static final Schema loadSchema (String path) throws URISyntaxException, SAXException {
        System.setProperty ("jdk.xml.maxOccurLimit", "100000");
        SchemaFactory schemaFactory = SchemaFactory.newInstance (XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL resource =  (ru.eludia.products.mosgis.ws.base.Error.EXP001000).getClass ().getClassLoader ().getResource ("META-INF/wsdl/" + path);
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

    protected final RequestHeader getRequestHeader () {

        HeaderList hl = (HeaderList) getContext ().getMessageContext ().get ("com.sun.xml.ws.api.message.HeaderList");
        
        if (hl == null) throw new IllegalStateException ("No SOAP header");

        if (hl.size () > 1) throw new IllegalStateException ("Too much SOAP headers");
    
        Header h = hl.get (0);

        try {
            return (RequestHeader) h.readAsJAXB (jc.createUnmarshaller ());
        }
        catch (JAXBException ex) {
            throw new IllegalStateException ("Cannot parse SOAP header", ex);
        }

    }
    
    protected final ru.gosuslugi.dom.schema.integration.base.AckRequest createAck (UUID uuid) {

        RequestHeader requestHeader = getRequestHeader ();
        
        AckRequest.Ack aa = of.createAckRequestAck ();

        aa.setMessageGUID (uuid.toString ());
        aa.setRequesterMessageGUID (requestHeader.getMessageGUID ());
        
        ru.gosuslugi.dom.schema.integration.base.AckRequest a = of.createAckRequest ();
        a.setAck (aa);

        return a;
        
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
    
}