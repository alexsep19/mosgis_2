package ru.eludia.products.mosgis.web;

import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;

public class FileSvcServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger (FileSvcServlet.class.getName ());
    
    private class RestFileException extends Exception {}
    private class FieldValidationException extends RestFileException {}
        
    @Override
    protected void doPut (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            String fn = request.getHeader ("X-Upload-Filename");
            if (fn == null || fn.isEmpty ()) throw new FieldValidationException ();
            response.setStatus (200);
            response.setHeader ("Location", "homemanagement/dc9441c7-312a-4210-b77f-ea368359795f");
            response.setHeader ("X-Upload-UploadID", "dc9441c7-312a-4210-b77f-ea368359795f");
        }
        catch (RestFileException ex) {
            logger.log (Level.WARNING, "Futile try to upload some file", ex);
            response.setStatus (400);
            response.setHeader ("X-Upload-Error", ex.getClass ().getSimpleName ());
        }

    }

    @Override
    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException ("No");
    }
    
    @Override
    protected void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException ("No");        
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
