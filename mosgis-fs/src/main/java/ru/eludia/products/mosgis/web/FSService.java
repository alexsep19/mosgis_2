package ru.eludia.products.mosgis.web;

import ru.eludia.products.mosgis.filestore.FileStoreLocal;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

@Stateless
@Path("/")
public class FSService {
    private static final Logger logger = Logger.getLogger(FSService.class.getName());

    private class RestFileException extends Exception {}
    private class FieldValidationException extends RestFileException {}
    private class ExtensionException extends RestFileException {}

    @EJB
    FileStoreLocal back;

    @PUT
    public Response uploadFile(@Context HttpServletRequest request) throws IOException {
        if (request.getContentLengthLong() <= 0) return Response.status(Response.Status.LENGTH_REQUIRED).build();
        String fn = request.getHeader("X-Upload-Filename");
        try {
            checkFileName(fn);
        } catch (RestFileException e) {
            return Response.status(Response.Status.BAD_REQUEST).header("X-Upload-Error", e.getClass().getSimpleName()).build();
        }
        UUID uuid = back.store((UUID)request.getSession().getAttribute(SecurityFilter.SENDER_UUID), fn, request.getInputStream());
        return Response.status(Response.Status.OK).header("Location", "homemanagement/" + uuid).header("X-Upload-UploadID", uuid.toString()).build();
    }

    @GET
    public Response downloadFile() {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    private void checkFileName(String fn) throws FieldValidationException, ExtensionException {
        logger.info("fn=" + fn);
        if (fn == null) throw new FieldValidationException();
        int pos = fn.lastIndexOf('.');
        if (pos < 1) throw new FieldValidationException();
        checkFileExt(fn.substring(pos + 1));
    }

    private void checkFileExt(String ext) throws ExtensionException {
        switch (ext.toLowerCase()) {
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
                throw new ExtensionException();
        }
    }
}