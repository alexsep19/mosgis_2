package ru.eludia.products.mosgis.rest.resources;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.ValidationException;

import ru.eludia.products.mosgis.rest.api.VocOverhaulWorkTypesLocal;
import ru.eludia.products.mosgis.rest.misc.EJBResource;

@Path("voc_overhaul_work_types")
public class VocOverhaulWorkTypes extends EJBResource <VocOverhaulWorkTypesLocal> {
    
    private void checkGet () {
        if (!securityContext.isUserInRole ("admin") && !securityContext.isUserInRole ("nsi_20_7")) throw new ValidationException ("foo", "Доступ запрещен");
    }
    
    private void checkPost () {
        if (!securityContext.isUserInRole ("nsi_20_7")) throw new ValidationException ("foo", "Доступ запрещен");
    }
    
    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) {
        checkGet ();
        return back.getItem (id, getUser ());
    }
    
    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        checkGet ();
        return back.select (p, getUser ()); 
    }
    
    @POST
    @Path("import") 
    @Produces (APPLICATION_JSON)
    public JsonObject doImport () {
        checkPost ();
        return back.doImport (getUser ());
    }
    
    @POST
    @Path("vocs") 
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs () {
        checkGet ();
        return back.getVocs ();
    }
    
}
