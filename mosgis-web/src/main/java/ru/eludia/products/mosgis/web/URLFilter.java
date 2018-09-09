package ru.eludia.products.mosgis.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;

public class URLFilter implements Filter {
    
    private FilterConfig filterConfig = null;
    private final static Logger logger = Logger.getLogger (Filter.class.getName ());
    private String ver = "";
    
    public URLFilter () {
    }    

    @Override
    public void doFilter (ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        RequestWrapper wrappedRequest = new RequestWrapper ((HttpServletRequest) request);
                        
        try {
            wrappedRequest.setResponseHeaders ((HttpServletResponse) response);
            chain.doFilter (wrappedRequest, response);
        }
        catch (Throwable t) {
            throw new ServletException (t);
        }
        
    }

    public FilterConfig getFilterConfig () {
        return (this.filterConfig);
    }

    public void setFilterConfig (FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public void destroy () {        
    }

    @Override
    public void init (FilterConfig filterConfig) {        
        
        this.filterConfig = filterConfig;

        try (InputStream inputStream = filterConfig.getServletContext ().getResourceAsStream ("/META-INF/MANIFEST.MF")) {                                
            ver = new Manifest (inputStream).getMainAttributes ().getValue ("Weblogic-Application-Version").toString ();
        }
        catch (IOException ex) {
            logger.log (Level.SEVERE, "Cannot read MANIFEST.MF", ex);
        }

    }

    @Override
    public String toString () {
        if (filterConfig == null) return ("URLFilter()");
        StringBuilder sb = new StringBuilder ("URLFilter(");
        sb.append (filterConfig);
        sb.append (")");
        return (sb.toString ());
        
    }

    public void log (String msg) {
        filterConfig.getServletContext ().log (msg);        
    }

    private static final Pattern RE_STATIC = Pattern.compile ("/__\\w+(/.*)");

    class RequestWrapper extends HttpServletRequestWrapper {
        
        private boolean isRest = false;
        private boolean isVersionedStatic = false;
        private String result;

        private static final String INDEX_HTML = "/index.html";
        private static final long MS_IN_YEAR = 365 * 24 * 60 * 60 * 1000L;
        
        void setResponseHeaders (HttpServletResponse hr) {

            if (isRest) 
                hr.setHeader ("X-Mosgis-Version", ver);            
            else 
                hr.setDateHeader ("Expires", 
                    isVersionedStatic ? System.currentTimeMillis () + MS_IN_YEAR : 
                    0L
                );

        }

        private boolean isRestRequest (String servletPath) {
            return !"GET".equals (getMethod ()) || servletPath.startsWith ("/_rest");
        }

        private String rewriteStaticPath (String servletPath) {
            Matcher m = RE_STATIC.matcher (servletPath);
            return 
                (isVersionedStatic = m.matches ()) ? "/_" + m.group (1) : 
                INDEX_HTML;
        }

        private String calcResult (String servletPath) {
            
            switch (servletPath) {
                case INDEX_HTML:     return servletPath;
                case "/favicon.ico": return "/_mandatory_content/favicon.ico";
                case "/robots.txt":  return "/_mandatory_content/robots.txt";
                default: return 
                    (isRest = isRestRequest (servletPath)) ? servletPath : 
                    rewriteStaticPath (servletPath);
            }            
            
        }
        
        public RequestWrapper (HttpServletRequest request) {
            super (request);
            result = calcResult (super.getServletPath ());
        }

        @Override
        public String getPathInfo () {
            return null; 
        }

        @Override
        public String getServletPath () {
            return result;
        }

        @Override
        public String toString () {
            return super.getServletPath () + " .. " + getServletPath () + ", isRest=" + isRest + ", isVersionedStatic=" + isVersionedStatic;
        }

        @Override
        public long getDateHeader (String name) {
            return 
                "If-Modified-Since".equals (name) ? 0L : // suppress 304 responses, always send content
                super.getDateHeader (name);
        }

    }
    
}