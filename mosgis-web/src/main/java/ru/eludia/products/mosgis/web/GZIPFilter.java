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
        
        public boolean checkResponseFormat () {
            switch(getHeader(HttpHeaders.CONTENT_TYPE)) {
                case "text/javascript":  
                case "text/css":         
                case "application/json": return true;
                
                    default:                 return false;
            }
        }
        
        @Override
        public ServletOutputStream getOutputStream () throws IOException {
            if (outputStream == null) {
                if (checkResponseFormat())
                    outputStream = (new ServletResponseGZIPOutputStream (httpServletResponse));
                else
                    outputStream = httpServletResponse.getOutputStream();
            }
            return outputStream;
        }
    }
  
    public class ServletResponseGZIPOutputStream  extends ServletOutputStream {
        
        protected GZIPOutputStream output;
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
            output.finish();
            byte[] bytes = baos.toByteArray();
            response.setContentLength(bytes.length);
            response.addHeader("Content-Encoding", GZIP);
            ServletOutputStream output0 = response.getOutputStream();
            output0.write(bytes);
            output0.flush();
            output0.close();
        }
        
        @Override
        public void flush () throws IOException {
            output.flush();
        }
        
        @Override
        public void write(byte[] b) throws IOException {
            output.write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            output.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            output.write(b);
        }
        
         @Override
        public void setWriteListener(WriteListener writeListener) {
        }
        
        @Override
        public boolean isReady() {
            return false;
        }
    }
}
