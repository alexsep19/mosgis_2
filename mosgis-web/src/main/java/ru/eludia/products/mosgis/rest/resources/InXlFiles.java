package ru.eludia.products.mosgis.rest.resources;

import java.io.OutputStream;
import java.net.URLEncoder;
import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import ru.eludia.products.mosgis.rest.api.InXlFilesLocal;

@Path ("in_xl_files")
public class InXlFiles extends EJBResource <InXlFilesLocal> {
    
    protected Response createErrDownloadResponse (String id, String fileName, int len) {
        
        try {
            return Response
                .ok ((StreamingOutput) (OutputStream output) -> {((InXlFilesLocal) back).download_errors (id, output);})
                .header ("Content-Disposition", "attachment;filename=" + URLEncoder.encode (fileName + "_(ошибки)", "UTF-8"))
                .header ("Content-Length", len)
                .build ();
            
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
    }
   

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) {
        return back.select (p, getUser ());
    }

    @POST
    @Path("create") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doCreate (JsonObject p) {
        return back.doCreate (p, getUser ());
    }
    
    @POST
    @Path("{id}/update") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doUpdate (@PathParam ("id") String id, JsonObject p) {
        return back.doUpdate (id, p, getUser ());
    }

    @POST
    @Path("{id}/delete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doDelete (@PathParam ("id") String id) {
        return back.doDelete (id, getUser ());
    }
    
    @POST
    @Path("{id}/download") 
    @Produces(APPLICATION_OCTET_STREAM)
    public Response download (@PathParam ("id") String id) {
        JsonObject item = back.getItem (id);
        return createFileDownloadResponse (id, item.getString ("label"), item.getInt ("len"));
    }
    
    @POST
    @Path("{id}/download_errors") 
    @Produces(APPLICATION_OCTET_STREAM)
    public Response download_errors (@PathParam ("id") String id) {
        JsonObject item = back.getItem (id);
        return createErrDownloadResponse (id, item.getString ("label"), item.getInt ("len"));
    }
    
    @POST
    @Path ("vocs")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs () { 
        return back.getVocs (); 
    }

}