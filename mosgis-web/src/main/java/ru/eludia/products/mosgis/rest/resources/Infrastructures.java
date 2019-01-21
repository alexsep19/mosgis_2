package ru.eludia.products.mosgis.rest.resources;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.InfrastructuresLocal;
import ru.eludia.products.mosgis.rest.misc.EJBResource;

@Path("infrastructures")
public class Infrastructures extends EJBResource<InfrastructuresLocal> {
    
    private void checkGet () {
        
        if (!securityContext.isUserInRole ("admin") &&
            !securityContext.isUserInRole ("nsi_20_2") &&
            !securityContext.isUserInRole ("nsi_20_7") &&
            !securityContext.isUserInRole ("nsi_20_8"))
            throw new ValidationException ("foo", "Доступ запрещен");
        
    }

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) {
        checkGet ();
        return back.select (p, getUser ()); 
    }

    @POST
    @Path ("vocs")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs () {
        checkGet ();
        return back.getVocs (); 
    }
    
    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
        checkGet ();
        return back.getItem (id);
    }
    
    @POST
    @Path("{id}/log") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        checkGet ();
        return back.getLog (id, p, getUser ());
    }
    
    @POST
    @Path("create") 
    @Produces (APPLICATION_JSON)
    public JsonObject doCreate (JsonObject p) {
        return back.doCreate (p, getUser());
    }
    
}
