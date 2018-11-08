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
import java.io.ByteArrayOutputStream;

public class GZIPFilter implements Filter {
    
    private static final String [] keys = new String [] {"text/javascript", "text/css", "application/json"};
    private final static Logger logger = Logger.getLogger (Filter.class.getName ());
    private static final String GZIP = "gzip";
    
    @Override
    public void init(FilterConfig filterConfig) {
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String acceptEncoding = httpRequest.getHeader(HttpHeaders.ACCEPT_ENCODING);
        
        try {
            if (acceptEncoding != null && acceptEncoding.indexOf(GZIP) >= 0) {
                
                GZIPResponseWrapper gzipResponse = new GZIPResponseWrapper(httpResponse);
                chain.doFilter(request, gzipResponse);
                gzipResponse.finish();
                return;
                
            } else {  
                chain.doFilter(request, response);
            }
        } catch (Throwable t) {
            throw new ServletException (t);
        }
    }

    @Override
    public void destroy() {
    }
    
    public class GZIPResponseWrapper extends HttpServletResponseWrapper {
        
        protected ServletOutputStream outputStream;
        protected HttpServletResponse httpServletResponse;
        
        public GZIPResponseWrapper(HttpServletResponse httpServletResponse) {
            super(httpServletResponse);
            this.httpServletResponse = httpServletResponse;
        }
        
        public void finish () throws IOException {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        
        @Override
        public void flushBuffer () throws IOException {
            outputStream.flush();
        }
        
        public boolean check () {
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
            return content;
        }
        
        @Override
        public ServletOutputStream getOutputStream () throws IOException {
            if (outputStream == null) {
                if (check())
                    outputStream = initGzip();
                else
                    outputStream = httpServletResponse.getOutputStream();
            }
            return outputStream;
        }
        public ServletOutputStream initGzip() throws IOException {
            return (new ServletResponseGZIPOutputStream (httpServletResponse));
        }
    }
    
    
    
    public class ServletResponseGZIPOutputStream  extends ServletOutputStream {
        
        protected GZIPOutputStream output;
        final AtomicBoolean open = new AtomicBoolean(true);
        protected ByteArrayOutputStream baos;
        protected HttpServletResponse response;
        
        public ServletResponseGZIPOutputStream (HttpServletResponse response) throws IOException {
            super();
            this.response = response;
            baos = new ByteArrayOutputStream();
            output = new GZIPOutputStream(baos);
        }
        @Override
        public void close () throws IOException {
            if (open.compareAndSet(true, false)) {
                output.finish();
                byte[] bytes = baos.toByteArray();
                response.setContentLength(bytes.length);
                response.addHeader("Content-Encoding", "gzip");

                ServletOutputStream output = response.getOutputStream();
                output.write(bytes);
                output.flush();
                output.close();
            }
        }
        
        @Override
        public void flush () throws IOException {
            if (!open.get()) {
                throw new IOException("Stream closed!");
            }
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
            return open.get();
        }
    }
}
