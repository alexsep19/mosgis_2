package ru.eludia.products.mosgis.ws.soap.impl.base;

import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFactory;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import ru.eludia.products.mosgis.jmx.Conf;

public class WSSUsernameTokenSecurityHandler implements SOAPHandler<SOAPMessageContext> {
    
    private final static Logger logger = Logger.getLogger (WSSUsernameTokenSecurityHandler.class.getName ());
    private static SOAPFactory factory;
    private final static DateFormat df = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    static {
        
        df.setTimeZone (TimeZone.getTimeZone ("UTC"));

        try {
            factory = SOAPFactory.newInstance();
        }
        catch (SOAPException ex) {
            throw new IllegalStateException (ex);
        }
        
    }

    private VocSetting.i login;
    private VocSetting.i pwd;

    public WSSUsernameTokenSecurityHandler (VocSetting.i login, VocSetting.i pwd) {
        this.login = login;
        this.pwd = pwd;
    }
            
    @Override
    public boolean handleMessage (SOAPMessageContext context) {
        if (((Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue ()) tryAddToken (context);
        return true;
    }

    private void tryAddToken (SOAPMessageContext context) {
        try {
            addToken (context);
        } catch (Exception e) {
            logger.log (Level.SEVERE, "Problem when setting WSSE header", e);
        }
    }

    private void addToken (SOAPMessageContext context) throws SOAPException, NoSuchAlgorithmException, IOException {
        
        context
            .getMessage  ()
            .getSOAPPart ()
            .getEnvelope ()
            .getHeader   ()
            .addChildElement (createSecurityEl ());
        
    }

    private SOAPElement createElement (String name) throws SOAPException {
        SOAPElement el = factory.createElement (name, "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        return el;
    }
    
    private SOAPElement createSecurityEl () throws SOAPException, NoSuchAlgorithmException, IOException {
        SOAPElement el = createElement ("Security");
        el.addChildElement (createTokenEl ());
        return el;
    }

    private SOAPElement createTokenEl () throws SOAPException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        
        byte[] nonceBytes = getNonce ();
        
        String createdDate = df.format (Calendar.getInstance ().getTime ());
        
        byte [] createdDateBytes = createdDate.getBytes ("UTF-8");        
        byte [] passwordBytes = Conf.get (pwd).getBytes ("UTF-8");        
        byte [] digestedPassword = getPasswordDigest (nonceBytes, createdDateBytes, passwordBytes);
                
        SOAPElement el = createElement ("UsernameToken");
        el.addChildElement (createUserEl ());
        el.addChildElement (createNonceEl   (Base64.getEncoder ().encodeToString (nonceBytes)));
        el.addChildElement (createPwdEl     (Base64.getEncoder ().encodeToString (digestedPassword)));
        el.addChildElement (createCreatedEl (createdDate));
        
        return el;
        
    }

    private byte[] getPasswordDigest (byte[] nonceBytes, byte[] createdDateBytes, byte[] passwordBytes) throws NoSuchAlgorithmException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write (nonceBytes);
        baos.write (createdDateBytes);
        baos.write (passwordBytes);
        MessageDigest md = MessageDigest.getInstance ("SHA-1");
        byte [] digestedPassword = md.digest (baos.toByteArray ());
        return digestedPassword;
    }

    private byte[] getNonce () throws NoSuchAlgorithmException {
        SecureRandom rand = SecureRandom.getInstance ("SHA1PRNG");
        rand.setSeed(System.currentTimeMillis());
        byte[] nonceBytes = new byte[16];
        rand.nextBytes (nonceBytes);
        return nonceBytes;
    }

    private SOAPElement createCreatedEl (String s) throws SOAPException {
        SOAPElement el = factory.createElement ("Created", "wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
        el.addTextNode (s);
        return el;
    }
    private SOAPElement createNonceEl (String s) throws SOAPException {
        SOAPElement el = createElement ("Nonce");
        el.addTextNode (s);
        return el;
    }

    private SOAPElement createPwdEl (String s) throws SOAPException {        
        SOAPElement el = createElement ("Password");
        el.addTextNode (s);
        el.addAttribute (QName.valueOf ("Type"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest");
        return el;
    }

    private SOAPElement createUserEl () throws SOAPException {
        SOAPElement el = createElement ("Username");
        el.addTextNode (Conf.get (login));
        return el;
    }

    @Override
    public Set<QName> getHeaders() {
        return new TreeSet ();
    }
    
    @Override
    public boolean handleFault (SOAPMessageContext context) {return false;}
    
    @Override
    public void close (MessageContext context) {}

}
