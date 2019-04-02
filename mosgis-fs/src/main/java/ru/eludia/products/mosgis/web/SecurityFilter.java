package ru.eludia.products.mosgis.web;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityFilter implements ContainerRequestFilter {

    private final static Logger logger = Logger.getLogger (SecurityFilter.class.getName ());
    
    @Context
    HttpServletRequest httpServletRequest;    
    
    @Override
    public void filter (ContainerRequestContext requestContext) throws IOException {
        
        String requestURI = httpServletRequest.getRequestURI ();
        
        if (requestURI != null && requestURI.endsWith ("/application.wadl")) return;
        
        HttpSession session = httpServletRequest.getSession (false);

        if (session == null) {
            
            if (!"sessions".equals (httpServletRequest.getParameter ("type"))) {
                
                logger.warning (getBlockReason (httpServletRequest));
                
                requestContext.abortWith (Response.status (UNAUTHORIZED).build ());
                
            }
            
        }
        else {

            requestContext.setSecurityContext (new SecurityCtx (
                (String)    session.getAttribute ("user.id"), 
                (String)    session.getAttribute ("user.label"), 
                (String)    session.getAttribute ("user.uuid_org"), 
                (Object []) session.getAttribute ("user.roles"))
            );
            
        }
        
    }

    private static String getBlockReason (HttpServletRequest r) {
        
        StringBuilder sb = new StringBuilder ("Blocked ");
        
        sb.append (r.getMethod ());

        sb.append (' ');
        
        sb.append (r.getRequestURI ());
        
        String queryString = r.getQueryString ();
        
        if (queryString != null && !queryString.isEmpty ()) {
            sb.append ('?');
            sb.append (queryString);
        }
        
        sb.append (" by ");
        
        String requestedSessionId = r.getRequestedSessionId ();
        if (requestedSessionId != null && !requestedSessionId.isEmpty ()) {
            sb.append (requestedSessionId);
        }
        else {
            sb.append ("some anonymous user");
        }

        return sb.toString ();
        
    }
    
}
