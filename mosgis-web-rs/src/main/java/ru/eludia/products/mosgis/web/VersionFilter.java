package ru.eludia.products.mosgis.web;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;

public class VersionFilter implements Filter {
    
    private FilterConfig filterConfig = null;
    private final static Logger logger = Logger.getLogger (VersionFilter.class.getName ());
    private String ver = "";
    
    protected static InitialContext ic;
    
    static {                
        try {
            ic = new InitialContext ();
        }
        catch (NamingException nex) {
            throw new IllegalStateException (nex);
        }        
    }

    public VersionFilter () {
    }    

    @Override
    public void doFilter (ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        final HttpServletRequest hr = (HttpServletRequest) request;

        RequestWrapper wrappedRequest = new RequestWrapper (hr);
                        
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
        
        try {
            ver = ic.lookup ("mosgis.confTopic").toString ().substring (7, 31);
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot get application version", ex);
        }
        
    }

    @Override
    public String toString () {
        if (filterConfig == null) return ("VersionFilter()");
        StringBuilder sb = new StringBuilder ("VersionFilter(");
        sb.append (filterConfig);
        sb.append (")");
        return (sb.toString ());
        
    }

    public void log (String msg) {
        filterConfig.getServletContext ().log (msg);        
    }

    class RequestWrapper extends HttpServletRequestWrapper {
        
        private String result;
        
        void setResponseHeaders (HttpServletResponse hr) {
            hr.setHeader ("X-Mosgis-Version", ver);            
        }

        private String calcResult (String servletPath) {
            return servletPath;
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
        public long getDateHeader (String name) {
            return 
                "If-Modified-Since".equals (name) ? 0L : // suppress 304 responses, always send content
                super.getDateHeader (name);
        }

    }
    
}