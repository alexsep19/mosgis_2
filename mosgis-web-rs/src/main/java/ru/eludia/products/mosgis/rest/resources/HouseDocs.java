package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import javax.ws.rs.core.Response;
import ru.eludia.products.mosgis.rest.api.HouseDocsLocal;

@Path ("house_docs")
public class HouseDocs extends EJBResource <HouseDocsLocal> {

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) {
        return back.select (p);
    }

    @POST
    @Path("create") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doCreate (JsonObject p) {
        return back.doCreate (p);
    }
    
    @POST
    @Path("{id}/update") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doUpdate (@PathParam ("id") String id, JsonObject p) {
        return back.doUpdate (id, p);
    }

    @POST
    @Path("{id}/edit") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doEdit (@PathParam ("id") String id, JsonObject p) {
        return back.doEdit (id, p);
    }

    @POST
    @Path("{id}/delete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doDelete (@PathParam ("id") String id) {
        return back.doDelete (id);
    }
    
    @POST
    @Path("{id}/download") 
    @Produces(APPLICATION_OCTET_STREAM)
    public Response download (@PathParam ("id") String id) {
        JsonObject item = back.getItem (id);
        return createFileDownloadResponse (id, item.getString ("label"), item.getInt ("len"));
    }

}