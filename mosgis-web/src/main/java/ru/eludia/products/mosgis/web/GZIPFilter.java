package ru.eludia.products.mosgis.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.core.HttpHeaders;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;

public class GZIPFilter implements Filter {
    
    private static final String [] KEYS = new String [] {"text/javascript", "text/css", "application/json"};
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
            if (contenttype == KEYS[0] || contenttype == KEYS[1] || contenttype == KEYS[2])
                content = true;
            return content;
        }
        
        @Override
        public ServletOutputStream getOutputStream () throws IOException {
            if (outputStream == null) {
                if (check())
                    outputStream = (new ServletResponseGZIPOutputStream (httpServletResponse));
                else
                    outputStream = httpServletResponse.getOutputStream();
            }
            return outputStream;
        }
    }
  
    public class ServletResponseGZIPOutputStream  extends ServletOutputStream {
        
        protected GZIPOutputStream output;
        protected Boolean open = true;
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
            if (open == true) {
                output.finish();
                byte[] bytes = baos.toByteArray();
                response.setContentLength(bytes.length);
                response.addHeader("Content-Encoding", GZIP);
                ServletOutputStream output = response.getOutputStream();
                output.write(bytes);
                output.flush();
                output.close();
                open = false;
            }
        }
        
        @Override
        public void flush () throws IOException {
            if (!open)
                throw new IOException("Stream closed!");
            output.flush();
        }
        
        @Override
        public void write(byte[] b) throws IOException {
            if (!open)
                throw new IOException("Stream closed!");
            output.write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (!open) 
                throw new IOException("Stream closed!");
            output.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            if (!open) {
                throw new IOException("Stream closed!");
            }
            output.write(b);
        }
         @Override
        public void setWriteListener(WriteListener writeListener) {
        }
        @Override
        public boolean isReady() {
            return open;
        }
    }
}
