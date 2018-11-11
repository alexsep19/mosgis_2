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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import ru.eludia.products.mosgis.proxy.GisWsAddress;
import ru.eludia.products.mosgis.proxy.ProxyLoggerLocal;

public class SvcServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger (SvcServlet.class.getName ());
    
    @EJB
    ProxyLoggerLocal back;
    
    private String getServiceName (String s) {
        switch (s) {
            case "AppealsAsync": return "AppealsServiceAsync";
            case "BillsAsync": return "BillsServiceAsync";
            case "Bills": return "BillsService";
            case "CapitalRepairAsync": return "CapitalRepairAsyncService";
            case "CapitalRepair": return "CapitalRepairService";
            case "DeviceMeteringAsync": return "DeviceMeteringServiceAsync";
            case "DeviceMetering": return "DeviceMeteringService";
            case "FASAsync": return "FASServiceAsync";
            case "HomeManagementAsync": return "HouseManagementServiceAsync";
            case "HomeManagement": return "HouseManagementService";
            case "InfrastructureAsync": return "InfrastructureServiceAsync";
            case "Infrastructure": return "InfrastructureService";
            case "InspectionAsync": return "InspectionServiceAsync";
            case "Inspection": return "InspectionService";
            case "LicensesAsync": return "LicenseServiceAsync";
            case "Licenses": return "LicenseService";
            case "MSPAsync": return "MSPAsyncService";
            case "MSP": return "MSPService";
            case "NsiCommonAsync": return "NsiServiceAsync";
            case "NsiCommon": return "NsiService";
            case "NsiAsync": return "NsiServiceAsync";
            case "Nsi": return "NsiService";
            case "OrgRegistryCommonAsync": return "RegOrgServiceAsync";
            case "OrgRegistryCommon": return "RegOrgService";
            case "OrgRegistryAsync": return "RegOrgServiceAsync";
            case "OrgRegistry": return "RegOrgService";
            case "PaymentAsync": return "PaymentsServiceAsync";
            case "RapAsync": return "RapServiceAsync";
            case "Rap": return "RapService";
            case "OrganizationAsync": return "ServicesServiceAsync";
            case "Organization": return "ServicesService";
            case "TariffAsync": return "TariffAsyncService";
            case "UkAsync": return "UkAsyncService";
            case "Uk": return "UkService";
            case "VolumeQualityAsync": return "VolumeQualityServiceAsync";
            case "VolumeQuality": return "VolumeQualityService";
            default: return s;
        }
    }
    

    private URL getURL (HttpServletRequest request, GisWsAddress gisWsAddress) throws MalformedURLException {
        StringBuilder sb = new StringBuilder (gisWsAddress.getUrl ());
        sb.append (request.getRequestURI ().substring (12));
        if (request.getQueryString () != null) {
            sb.append ('?');
            sb.append (request.getQueryString ());
        }
        URL url = new URL (sb.toString ());
        return url;
    }

    private static String getAuth (GisWsAddress gisWsAddress) throws UnsupportedEncodingException {
        return "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary ((gisWsAddress.getLogin () + ':' + gisWsAddress.getPassword ()).getBytes ("UTF-8"));
    }
    
    private void setAuth (HttpURLConnection con, GisWsAddress gisWsAddress) throws UnsupportedEncodingException {
        if (gisWsAddress.getLogin ().isEmpty ()) return;
        con.setRequestProperty ("Authorization", getAuth (gisWsAddress));
    }
    
    private void pump (final InputStream is, final OutputStream os) throws IOException {
        byte [] buffer = new byte [8 * 1024];
        int len;
        while ((len = is.read (buffer)) > 0) os.write(buffer, 0, len);
    }

    private HttpURLConnection createConnection (HttpServletRequest request) throws MalformedURLException, ProtocolException, UnsupportedEncodingException, IOException {
        GisWsAddress gisWsAddress = back.getGisWsAddress ();
        URL url = getURL (request, gisWsAddress);
        HttpURLConnection con = (HttpURLConnection) url.openConnection ();
        final String method = request.getMethod ();
        con.setRequestMethod (method);
        logger.info (method + ' ' + url);
        setAuth (con, gisWsAddress);
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
    private static final String BODY_TOKEN = ":Body>";
    private static final String STATE_TOKEN = ":RequestState>";
    
    Pattern RE_ERR = Pattern.compile  (":ErrorCode>([^<]*)</\\w+:ErrorCode>\\s*<\\w+:(Description|ErrorMessage)>([^<]*)<", Pattern.MULTILINE);

    @Override
    protected void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpURLConnection con = createConnection (request);
        
        String url = con.getURL ().toString ();
        String svcName = getServiceName (url.substring (url.lastIndexOf ('/') + 1));
        
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
            pump (is, cs, sb);            
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
        
        StringBuilder osb = new StringBuilder ();
        
        try (InputStream is = con.getInputStream ()) {
            pump (is, "UTF-8", osb);
        }
        
        String os = osb.toString ();
        
        if ("getState".equals (methodName)) {
            
            String omsg = "";
            int ompos = s.indexOf (MSG_TOKEN, s.indexOf (BODY_TOKEN));
            if (ompos >= 0) {
                ompos += MSG_TOKEN.length ();
                omsg = s.substring (ompos, ompos + 36);
            }

            String state = "";
            int stpos = os.indexOf (STATE_TOKEN);
            if (stpos >= 0) {
                stpos += STATE_TOKEN.length ();
                state = os.substring (stpos, stpos + 1);
            }
            
            String err="";
            String dsc="";
            
            if ("3".equals (state)) {
                
                final Matcher matcher = RE_ERR.matcher (os);
                
                if (matcher.find ()) {
                    err = matcher.group (1);
                    dsc = matcher.group (3);
                }
                
                back.logResponse (omsg, os, err, dsc);
            
            }
            else {
                
                back.logPending (omsg);
                
            }

logger.info ("msg=" + omsg + ", state=" + state + ", err=" + err + ", dsc=" + dsc);
            
        }
        else {
            
            String ack = "";
            final int bpos = os.indexOf (BODY_TOKEN);
            int ompos = os.indexOf (MSG_TOKEN, bpos);
            if (ompos >= 0) {
                ompos += MSG_TOKEN.length ();
                ack = os.substring (ompos, ompos + 36);
            }
            
logger.info ("ack=" + ack);

            back.logRequest (msg, svcName, methodName, s, org, ack);

        }

        response.setCharacterEncoding ("UTF-8");
        try (PrintWriter w = response.getWriter ()) {
            w.print (os);
        }
        
    }

    private void pump (final InputStream is, String cs, StringBuilder sb) throws IOException {
        try (InputStreamReader isr = new InputStreamReader (is, cs)) {
            try (BufferedReader br = new BufferedReader (isr)) {
                while (true) {
                    String line = br.readLine ();
                    if (line == null) break;
                    sb.append (line);
                    sb.append ('\n');
                }
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
