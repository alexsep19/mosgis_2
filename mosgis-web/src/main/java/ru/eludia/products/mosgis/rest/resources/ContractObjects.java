package ru.eludia.products.mosgis.rest.resources;

import javax.annotation.security.RolesAllowed;
import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import javax.ws.rs.core.SecurityContext;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.ContractObjectsLocal;

@Path ("contract_objects")
public class ContractObjects extends EJBResource <ContractObjectsLocal> {

    @Context SecurityContext securityContext;
    
    private User getUser () {
        return (User) securityContext.getUserPrincipal ();
    }

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) {
        return back.select (p, getUser ());
    }

    @POST
    @Path("create") 
    @RolesAllowed ("nsi_20_1")
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

}