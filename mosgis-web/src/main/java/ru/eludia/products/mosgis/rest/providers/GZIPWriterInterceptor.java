package ru.eludia.products.mosgis.rest.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

@Provider
public class GZIPWriterInterceptor implements WriterInterceptor {

    private final static Logger logger = Logger.getLogger (GZIPWriterInterceptor.class.getName ());

    private static final String GZIP = "gzip";
    
    @Context
    private HttpHeaders headers;
    
    @Override
    public void aroundWriteTo (WriterInterceptorContext ctx) throws IOException, WebApplicationException {

        boolean isToZip = false;
        
        if (ctx.getHeaders ().getFirst ("Content-Disposition") == null) {
            List<String> acceptEncoding = headers.getRequestHeader (HttpHeaders.ACCEPT_ENCODING);        
            if (acceptEncoding != null) for (String i: acceptEncoding) if (i.contains (GZIP)) isToZip = true;
        }
        
        if (!isToZip) {
            ctx.proceed (); 
            return;
        }

        OutputStream old = ctx.getOutputStream (); 
        
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream (old); 

        ctx.setOutputStream (gzipOutputStream);        
        ctx.getHeaders ().add (HttpHeaders.CONTENT_ENCODING, GZIP);
                
        try { 
            ctx.proceed (); 
        } 
        finally { 
            gzipOutputStream.finish (); 
            ctx.setOutputStream (old); 
        }
    
    }    

}