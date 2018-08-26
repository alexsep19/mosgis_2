package ru.eludia.products.mosgis.rest.misc;

import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
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
    
//    private static final String JNDI_ROOT = "java:global/ru/eludia/products_mosgis-ear_ear_1/0-SNAPSHOT/mosgis-ejb-1/0-SNAPSHOT";
    private static final String JNDI_ROOT = "java:global/mosgis-ear-1/0-SNAPSHOT/mosgis-ejb-1/0-SNAPSHOT";

    protected T getEjbRef () {
        
        Class c = getEJBClass ();
        String name = c2n.get (c);
        
        try {
            
            if (name != null) return (T) ic.lookup (name);
            
            NamingEnumeration<NameClassPair> list = ic.list (JNDI_ROOT);
            
            while (list.hasMore()) {
                
                final NameClassPair pair = list.next ();
                final String n = pair.getName ();

                if (n.endsWith ("!ru")) continue;
                
                name = JNDI_ROOT + '/' + n;
               
                Object o = ic.lookup (name);
                
                if (!c.isAssignableFrom (o.getClass ())) continue;
                
                c2n.putIfAbsent (c, name);
                
                return (T) o;
                
            }
            
        }
        catch (NamingException nex) {
            throw new IllegalArgumentException (nex);
        }
        
        throw new IllegalStateException ("EJB of type " + c.getName () + " is not found");
        
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

    protected T back = getEjbRef ();
    
}