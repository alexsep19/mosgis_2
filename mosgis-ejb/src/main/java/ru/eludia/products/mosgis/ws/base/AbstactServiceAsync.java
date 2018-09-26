package ru.eludia.products.mosgis.ws.base;

import javax.xml.ws.WebServiceContext;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Header;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.UUID;
import javax.ejb.EJB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
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
                    ru.gosuslugi.dom.schema.integration.organizations_registry_common.GetStateResult.class
            );
        }
        catch (JAXBException ex) {
            throw new IllegalStateException ("Cannot create JAXBContext", ex);
        }

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

    public static final Object getHeader (SOAPMessage msg, Class t) {
        try {            
            
            Iterator it = msg.getSOAPHeader ().examineAllHeaderElements ();

            while (it.hasNext ()) {

                try {
                    
                    Object o = jc.createUnmarshaller ().unmarshal ((SOAPHeaderElement) it.next ());

                    if (t.isAssignableFrom (o.getClass ())) return o;
                
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