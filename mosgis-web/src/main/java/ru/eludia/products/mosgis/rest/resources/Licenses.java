package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.api.LicenseLocal;

@Path ("licenses")
public class Licenses extends EJBResource <LicenseLocal> {

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        return back.select (p); 
    }
    
    @POST
    @Path("vocs") 
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs () { 
        return back.getVocs (); 
    }        

    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
        return back.getItem (id, getUser ());
    }
    
    @POST
    @Path("{id}/documents")
    @Produces(APPLICATION_JSON)
    public JsonObject getDocuments(@PathParam("id") String id, JsonObject p) {
        return back.getDocuments(id, p);
    }

    @POST
    @Path("{id}/houses")
    @Produces(APPLICATION_JSON)
    public JsonObject getHouses(@PathParam("id") String id, JsonObject p) {
        return back.getHouses(id, p);
    }

    @POST
    @Path("{id}/log") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        return back.getLog (id, p, getUser ());
    }
    
}