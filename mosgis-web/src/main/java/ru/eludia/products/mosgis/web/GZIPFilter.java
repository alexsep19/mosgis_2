package ru.eludia.products.mosgis.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.core.HttpHeaders;
import javax.servlet.WriteListener;

public class GZIPFilter implements Filter {
    
    private static final String [] keys = new String [] {"text/javascript", "text/css", "application/json"};
    //private String ver = "";
    private final static Logger logger = Logger.getLogger (Filter.class.getName ());
    //private FilterConfig filterConfig = null;
    //protected static InitialContext ic;
    private static final String GZIP = "gzip";
            
    /*static {                
        try {
            ic = new InitialContext ();
        }
        catch (NamingException nex) {
            throw new IllegalStateException (nex);
        }        
    }*/
    
    @Override
    public void init(FilterConfig filterConfig) {
        /*try {
            ver = ic.lookup ("mosgis.confTopic").toString ().substring (7, 31);
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot get application version", ex);
        }*/
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        logger.log (Level.INFO, "GZIP FILTER STARTED");
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String acceptEncoding = httpRequest.getHeader(HttpHeaders.ACCEPT_ENCODING);
        
        try {
            if (acceptEncoding != null && acceptEncoding.indexOf(GZIP) >= 0) {
                
                GZIPResponseWrapper gzipResponse = new GZIPResponseWrapper(httpResponse);
                //logger.log (Level.INFO, "GZIP FILTER continues");
                chain.doFilter(request, gzipResponse);
                //logger.log (Level.INFO, "GZIP FILTER FINISHED 1");
                gzipResponse.finish();
                
            } else {  
                chain.doFilter(request, response);
            }
        } catch (Throwable t) {
            throw new ServletException (t);
        }
        logger.log (Level.INFO, "GZIP FILTER FINISHED 2");
    }

    @Override
    public void destroy() {
        logger.log (Level.INFO, "GZIP FILTER destroied");
    }
    /*
    public FilterConfig getFilterConfig () {
        return (this.filterConfig);
    }
    
    public void setFilterConfig (FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }
    
    public void log (String msg) {
        filterConfig.getServletContext ().log (msg);        
    }
    @Override
    public String toString () {
        if (filterConfig == null) return ("GZIPFilter()");
        StringBuilder sb = new StringBuilder ("GZIPFilter(");
        sb.append (filterConfig);
        sb.append (")");
        return (sb.toString ());  
    }*/

    
    public class GZIPResponseWrapper extends HttpServletResponseWrapper {

        private ServletResponseGZIPOutputStream gzipStream;
        private ServletOutputStream outputStream;
        
        public GZIPResponseWrapper(HttpServletResponse response) throws IOException {
            super(response);
        }
        
        public void finish() throws IOException {
            if (outputStream != null) {
                outputStream.close();
            }
            if (gzipStream != null) {
                gzipStream.close();
            }
        }

        @Override
        public void flushBuffer() throws IOException {
            if (outputStream != null) {
                outputStream.flush();
            }
            super.flushBuffer();
        }

        @Override
        public final ServletOutputStream getOutputStream() throws IOException {
            if (outputStream == null) {
                if (initGzip())
                    outputStream = gzipStream;
            }
            return outputStream;
        }

        @Override
        public void setContentLength(int len) {
        }

        private boolean initGzip() throws IOException {
            boolean content = false;
            String contenttype = getHeader(HttpHeaders.CONTENT_TYPE);
            
            if (contenttype != null) {
                for (String j : keys) {
                    if (contenttype.indexOf(j) != -1) {
                        content = true;
                        break;
                    }
                }
            }
            if (content) {
                setHeader(HttpHeaders.CONTENT_ENCODING, GZIP);
                gzipStream = new ServletResponseGZIPOutputStream(getResponse().getOutputStream());
                return true;
            } else {
                outputStream = getResponse().getOutputStream();
                return false;
            }
        }
    }

    public class ServletResponseGZIPOutputStream extends ServletOutputStream {
        
        GZIPOutputStream output;
        final AtomicBoolean open = new AtomicBoolean(true);
        //ServletOutputStream output;
        // GZIPOutputStream gzipoutput;
        public ServletResponseGZIPOutputStream(ServletOutputStream output) throws IOException {
            //this.output = output;
            this.output = new GZIPOutputStream(output);
        }

        @Override
        public void close() throws IOException {
            if (open.compareAndSet(true, false)) {
                output.close();
            }
        }

        @Override
        public void flush() throws IOException {
            output.flush();
        }

        @Override
        public void write(byte[] b) throws IOException {
            if (!open.get()) {
                throw new IOException("Stream closed!");
            }
            output.write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (!open.get()) {
                throw new IOException("Stream closed!");
            }
            output.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            if (!open.get()) {
                throw new IOException("Stream closed!");
            }
            output.write(b);
        }
        @Override
        public void setWriteListener(WriteListener writeListener) {
            
        }
        @Override
        public boolean isReady() {
            return (open.get());
        }
    }
}
