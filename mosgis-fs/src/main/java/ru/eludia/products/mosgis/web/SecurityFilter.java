package ru.eludia.products.mosgis.web;

import ru.eludia.products.mosgis.filestore.FileStoreLocal;

import javax.annotation.Priority;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Logger;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityFilter implements ContainerRequestFilter {

    private final static Logger logger = Logger.getLogger (SecurityFilter.class.getName ());

    private class AnonException extends Exception {}
    private class AuthException extends Exception {}

    private static final InitialContext ic;
    static {
        try {
            ic = new InitialContext ();
        }
        catch (NamingException nex) {
            throw new IllegalStateException (nex);
        }
    }

    @Context
    HttpServletRequest httpServletRequest;

    @Override
    public void filter (ContainerRequestContext requestContext) {
        String requestURI = httpServletRequest.getRequestURI ();
        if (requestURI != null && requestURI.endsWith ("/application.wadl")) return;
        try {
            try {
                requestContext.setSecurityContext(new AuthorizationContext(getSengerUuid(httpServletRequest.getHeader("Authorization"),
                        (FileStoreLocal) ic.lookup("java:app//mosgis-ejb/" + FileStoreLocal.class.getSimpleName().replace("Local", "Impl")))));
            } catch (AnonException e) {
                requestContext.getHeaders().add("WWW-Authenticate", "Basic realm=\"mosgis\"");
                requestContext.abortWith (Response.status (Response.Status.UNAUTHORIZED).build());
            } catch (AuthException e) {
                requestContext.abortWith (Response.status (Response.Status.FORBIDDEN).build());
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private UUID getSengerUuid(String auth, FileStoreLocal back) throws AnonException, AuthException {

        if (auth == null || auth.isEmpty()) throw new AnonException();

        String lp = new String(Base64.getDecoder().decode(auth.substring(6).getBytes()));
        int p = lp.indexOf(':');
        if (p < 1) throw new AnonException();

        UUID senderUuid = back.getSenderUuid(lp.substring(0, p), lp.substring(p + 1));

        if (senderUuid == null) throw new AuthException();

        return senderUuid;

    }
}
