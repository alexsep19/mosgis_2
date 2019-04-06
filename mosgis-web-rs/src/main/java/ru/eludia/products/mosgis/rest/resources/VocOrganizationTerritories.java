package ru.eludia.products.mosgis.rest.resources;

import javax.json.JsonObject;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.VocOrganizationTerritoriesLocal;
import ru.eludia.products.mosgis.rest.misc.EJBResource;

@Path("voc_organization_territories")
public class VocOrganizationTerritories extends EJBResource<VocOrganizationTerritoriesLocal> {

    private void checkAdmin () {
        if (!securityContext.isUserInRole("admin")) throw new ValidationException ("foo", "Доступ запрещён");
    }
    
    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        return back.select (p, getUser ());
    }
    
    @POST
    @Path("create") 
    @Produces (APPLICATION_JSON)
    public JsonObject doCreate (JsonObject p) {
        checkAdmin ();
        return back.doCreate (p, getUser ());
    }
    
    @POST
    @Path("{id}/delete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doDelete (@PathParam ("id") String id) {
        checkAdmin ();
        return back.doDelete (id, getUser ());
    }
    
}
