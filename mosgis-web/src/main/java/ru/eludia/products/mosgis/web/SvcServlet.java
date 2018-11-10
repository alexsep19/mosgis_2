package ru.eludia.products.mosgis.web;

import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;

public class SvcServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger (SvcServlet.class.getName ());

    private URL getURL (HttpServletRequest request) throws MalformedURLException {
        StringBuilder sb = new StringBuilder ("http://gis.dovsyanko/");
        sb.append (request.getRequestURI ().substring (12));
        if (request.getQueryString () != null) {
            sb.append ('?');
            sb.append (request.getQueryString ());
        }
        URL url = new URL (sb.toString ());
        return url;
    }

    private static String getAuth () throws UnsupportedEncodingException {
        return "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary ("sit:xw{p&&Ee3b9r8?amJv*]".getBytes ("UTF-8"));
    }
    
    private void setAuth (HttpURLConnection con) throws UnsupportedEncodingException {
        con.setRequestProperty ("Authorization", getAuth ());
    }
    
    private void pump (final InputStream is, final OutputStream os) throws IOException {
        byte [] buffer = new byte [8 * 1024];
        int len;
        while ((len = is.read (buffer)) > 0) os.write(buffer, 0, len);
    }

    private HttpURLConnection createConnection (HttpServletRequest request) throws MalformedURLException, ProtocolException, UnsupportedEncodingException, IOException {
        URL url = getURL (request);
        HttpURLConnection con = (HttpURLConnection) url.openConnection ();
        final String method = request.getMethod ();
        con.setRequestMethod (method);
        logger.info (method + ' ' + url);
        setAuth (con);
        return con;
    }

    private void copyHeader (HttpServletResponse response, final String name, HttpURLConnection con) {
        response.setHeader (name, con.getHeaderField (name));
    }
    
    private void copyHeader (HttpServletRequest request, final String name, HttpURLConnection con) {
        con.setRequestProperty (name, request.getHeader (name));
    }

    @Override
    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpURLConnection con = createConnection (request);

        int rc = con.getResponseCode ();        
        response.setStatus (rc);

logger.info ("rc=" + rc);
        
        copyHeader (response, "Content-Type", con);

        try (InputStream is = con.getInputStream ()) {            
            try (ServletOutputStream os = response.getOutputStream ()) {
                pump (is, os);
            }            
        }

    }
    
    private static final String CHARSET_TOKEN = ";charset=";
    private static final String ORG_TOKEN = ":orgPPAGUID>";
    private static final String MSG_TOKEN = ":MessageGUID>";

    @Override
    protected void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpURLConnection con = createConnection (request);
        
        String url = con.getURL ().toString ();
        String svcName = url.substring (url.lastIndexOf ('/') + 1);
        
        con.setDoOutput (true);
        copyHeader (request, "Content-Type", con);
        copyHeader (request, "SOAPAction", con);
        
        final String soapAction = request.getHeader ("SOAPAction");        
        String methodName = soapAction.substring (5, soapAction.length () - 1);
        
        String contentType = request.getHeader ("Content-Type");

        String cs = "UTF-8";

        try {
            int pos = contentType.indexOf (CHARSET_TOKEN);
            if (pos >= 0) cs = contentType.substring (pos + CHARSET_TOKEN.length ());
        }
        catch (Exception ex) {
            logger.log (Level.WARNING, contentType, ex);
        }        
        
        StringBuilder sb = new StringBuilder ();
        
        try (ServletInputStream is = request.getInputStream ()) {            
            try (InputStreamReader isr = new InputStreamReader (is, cs)) {                
                try (BufferedReader br = new BufferedReader (isr)) {                                            
                    while (true) {
                        String line = br.readLine ();
                        if (line == null) break;                        
                        sb.append (line);
                    }                                                               
                }                
            }            
        }
    
        String s = sb.toString ();
        
        String org = "";
        int opos = s.indexOf (ORG_TOKEN);
        if (opos >= 0) {
            opos += ORG_TOKEN.length ();
            org = s.substring (opos, opos + 36);
        }
        
        String msg = "";
        int mpos = s.indexOf (MSG_TOKEN);
        if (mpos >= 0) {
            mpos += MSG_TOKEN.length ();
            msg = s.substring (mpos, mpos + 36);
        }
    
        try (OutputStream os = con.getOutputStream ()) {
            os.write (s.getBytes (cs));
        }

        int rc = con.getResponseCode ();        
        response.setStatus (rc);

logger.info (svcName + '.' + methodName + ", org=" + org + ", msg=" + msg + ", rc=" + rc);
        
        copyHeader (response, "Content-Type", con);

        try (InputStream is = con.getInputStream ()) {            
            try (ServletOutputStream os = response.getOutputStream ()) {
                pump (is, os);
            }            
        }
        
    }

    @Override
    protected void doPut (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpURLConnection con = createConnection (request);
        con.setDoOutput (true);
        
        copyHeader (request, "Content-Length", con);
        copyHeader (request, "Content-MD5", con);
        copyHeader (request, "X-Upload-Filename", con);
        copyHeader (request, "X-Upload-Length", con);
        copyHeader (request, "X-Upload-OrgPPAGUID", con);
        copyHeader (request, "X-Upload-Part-Count", con);
        copyHeader (request, "X-Upload-Partnumber", con);
        
        try (ServletInputStream is = request.getInputStream ()) {
            try (OutputStream os = con.getOutputStream ()) {
                pump (is, os);
            }
        }

        int rc = con.getResponseCode ();        
        response.setStatus (rc);

logger.info ("rc=" + rc);
        
        copyHeader (response, "Content-Type", con);

        try (InputStream is = con.getInputStream ()) {            
            try (ServletOutputStream os = response.getOutputStream ()) {
                pump (is, os);
            }            
        }

    }

    @Override
    protected void doDelete (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new ServletException ("No");
    }

    @Override
    protected void doHead (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new ServletException ("No");
    }

    @Override
    protected void doOptions (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new ServletException ("No");
    }

    @Override
    protected void doTrace (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new ServletException ("No");
    }   

    @Override
    public String getServletInfo () {
        return "Services";
    }

}
