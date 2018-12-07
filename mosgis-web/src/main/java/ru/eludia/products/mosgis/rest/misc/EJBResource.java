package ru.eludia.products.mosgis.rest.misc;

import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.FileBackend;

public abstract class EJBResource<T> {
    
    private static final ConcurrentMap<Class, String> c2n = new ConcurrentHashMap <> ();
    
    protected final Logger logger = Logger.getLogger (getClass ().getName ());
        
    private final Class getEJBClass () {
        return (Class) ((ParameterizedType)getClass ().getGenericSuperclass ()).getActualTypeArguments () [0];
    }
    
    protected static InitialContext ic;
    
    static {                
        try {
            ic = new InitialContext ();
        }
        catch (NamingException nex) {
            throw new IllegalStateException (nex);
        }        
    }

    protected T getEjbRef () {
        
        Class c = getEJBClass ();
        
        c2n.computeIfAbsent (c, (x) -> "java:app//mosgis-ejb/" + x.getSimpleName ().replace ("Local", "Impl"));

        try {                        
            return (T) ic.lookup (c2n.get (c));                        
        }
        catch (NamingException nex) {
            throw new IllegalArgumentException (nex);
        }
                
    }
    
    protected Response createFileDownloadResponse (String id, String fileName, int len) {
        
        try {
            return Response
                .ok ((StreamingOutput) (OutputStream output) -> {((FileBackend) back).download (id, output);})
                .header ("Content-Disposition", "attachment;filename=" + URLEncoder.encode (fileName, "UTF-8"))
                .header ("Content-Length", len)
                .build ();
            
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
    }
    protected @Context ContainerRequestContext context;
    protected SecurityCtx securityContext = (SecurityCtx) context.getSecurityContext ();
    
    protected final User getUser () {
        return (User) securityContext.getUserPrincipal ();
    }
    
    protected T back = getEjbRef ();
    
}