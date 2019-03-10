package ru.eludia.products.mosgis.web;

import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.EJB;
import ru.eludia.products.mosgis.filestore.FileStoreLocal;

public class FileSvcServlet extends HttpServlet {
    
    @EJB
    FileStoreLocal back;
    
    private static final Logger logger = Logger.getLogger (FileSvcServlet.class.getName ());
    
    private class RestFileException extends Exception {}
    private class FieldValidationException extends RestFileException {}
    private class InvalidSizeException extends RestFileException {}
    private class ExtensionException extends RestFileException {}
         
    @Override
    protected void doPut (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {            
            if (request.getContentLengthLong () <= 0) throw new InvalidSizeException ();
            String fn = request.getHeader ("X-Upload-Filename");
            checkFileName (fn);
            UUID uuid = back.store (fn, request.getInputStream ());
            response.setStatus (200);
            response.setHeader ("Location", "homemanagement/" + uuid);
            response.setHeader ("X-Upload-UploadID", uuid.toString ());
        }
        catch (RestFileException ex) {
            logger.log (Level.WARNING, "Futile try to upload some file", ex);
            response.setStatus (400);
            response.setHeader ("X-Upload-Error", ex.getClass ().getSimpleName ());
        }

    }

    public void checkFileName (String fn) throws FieldValidationException, ExtensionException {
        logger.info ("fn=" + fn);
        if (fn == null) throw new FieldValidationException ();
        int pos = fn.lastIndexOf ('.');
        if (pos < 1) throw new FieldValidationException ();
        checkFileExt (fn.substring (pos + 1));
    }

    private void checkFileExt (String ext) throws ExtensionException {
        ext = ext.toLowerCase ();
        logger.info ("ext=" + ext);
        switch (ext) {
            case "pdf":
            case "xls":
            case "xlsx":
            case "doc":
            case "docx":
            case "rtf":
            case "jpg":
            case "jpeg":
            case "tif":
            case "tiff":
            case "zip":
            case "xml":
            case "rptdesign":
            case "crt":
            case "cer":
                return;
            default:
                throw new ExtensionException ();
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
