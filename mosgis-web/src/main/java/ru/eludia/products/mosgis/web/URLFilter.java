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
    public void doFilter (ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        RequestWrapper wrappedRequest = new RequestWrapper ((HttpServletRequest) request);
                        
        try {
            ((HttpServletResponse) response).setHeader ("X-Mosgis-Version", ver);
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

    private static final Pattern reStatic = Pattern.compile ("/__\\w+(/.*)");

    class RequestWrapper extends HttpServletRequestWrapper {
        
        public RequestWrapper (HttpServletRequest request) {
            super (request);
        }

        @Override
        public String getPathInfo () {
            return null; 
        }

        @Override
        public String getServletPath () {
            
            String s = super.getServletPath ();

            if (!"GET".equals (getMethod ())) return s;
            
            if (s.startsWith ("/_rest")) return s;

            if ("/favicon.ico".equals (s)) return "/_mandatory_content/favicon.ico";
            if ("/robots.txt".equals  (s)) return "/_mandatory_content/robots.txt";
            
            Matcher m = reStatic.matcher (s); 
                        
            if (m.matches ()) return "/_" + m.group (1);
                        
            return "/index.html"; 

        }

    }
    
}