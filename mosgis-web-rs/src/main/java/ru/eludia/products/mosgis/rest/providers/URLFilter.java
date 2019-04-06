package ru.eludia.products.mosgis.rest.providers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;

@Provider
@PreMatching
public class URLFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private final static Logger logger = Logger.getLogger (URLFilter.class.getName ());
    
    @Context
    HttpServletRequest request;    

    private static final Charset UTF8 = Charset.forName ("UTF-8");
    
    private static final String LOG_SIGNATURE = "signature";
    private static final String TS_START_REQUEST = "ts";

    private static final String [] keys = new String [] {"type", "id", "action", "part"};

    @Override
    public void filter (ContainerRequestContext requestContext) throws IOException {
        
        String s = request.getParameter ("type");
        
        if (s == null || s.isEmpty ()) return;
                
        UriBuilder b = requestContext.getUriInfo ().getAbsolutePathBuilder ();
        
        for (String key: keys) {
            String param = request.getParameter (key);
            if (param == null || param.isEmpty ()) continue;
            b.path (param);
        }
                
        requestContext.setRequestUri (b.build ());        

        byte [] body = new byte [] {};
        
        if (requestContext.getLength () < 4096) try (ByteArrayOutputStream baos = new ByteArrayOutputStream ()) {

            try (InputStream inputStream = requestContext.getEntityStream ()) {
                byte [] buffer = new byte [1024];
                int length;
                while ((length = inputStream.read (buffer)) != -1) baos.write (buffer, 0, length);
            }
            
            body = baos.toByteArray ();
            
            requestContext.setEntityStream (new ByteArrayInputStream (body));

        }

        HttpSession session = request.getSession (false);
                
        StringBuilder sb = new StringBuilder (session == null ? "NO_SESSION" : session.getId ());
        sb.append (' ');
        sb.append (session == null ? "0" : session.getAttribute ("user.id"));
        sb.append (" /");
        sb.append (requestContext.getUriInfo ().getPath ());
        sb.append (' ');        
        sb.append (new String (body, UTF8));

        String signature = sb.toString ();                

        logger.info (signature);
        
        requestContext.setProperty (TS_START_REQUEST, System.currentTimeMillis ());
        requestContext.setProperty (LOG_SIGNATURE, signature);
        
    }

    @Override
    public void filter (ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        
        final Object sign = requestContext.getProperty (LOG_SIGNATURE);
        
        if (sign == null) return;
        
        StringBuilder sb = new StringBuilder (sign.toString ());
        sb.append (' ');
        sb.append (System.currentTimeMillis () - (Long) requestContext.getProperty (TS_START_REQUEST));
        sb.append (" ms");        

        logger.info (sb.toString ());

    }
    
}