package ru.eludia.products.mosgis.rest.resources;

import javax.annotation.security.RolesAllowed;
import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.ContractObjectsLocal;

@Path ("contract_objects")
public class ContractObjects extends EJBResource <ContractObjectsLocal> {
    
    private String getUserOrg () {

        String userOrg = getUser ().getUuidOrg ();

        if (userOrg == null) {
            logger.warning ("User has no org set, access prohibited");
            throw new ValidationException ("foo", "Доступ запрещён");
        }

        return userOrg;
        
    }    
    
    private void checkOrg (JsonObject item) {

        String itemOrg = item.getString ("ctr.uuid_org", null);

        if (itemOrg == null) throw new InternalServerErrorException ("Wrong MgmtContract, no org: " + item);

        String userOrg = getUserOrg ();

        if (!userOrg.equals (itemOrg)) {
            logger.warning ("Org mismatch: " + userOrg + " vs. " + itemOrg);
            throw new ValidationException ("foo", "Доступ запрещён");
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
    
    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
        final JsonObject item = back.getItem (id);
        if (!securityContext.isUserInRole ("admin") && !securityContext.isUserInRole ("nsi_20_4")) checkOrg (item.getJsonObject ("item"));
        return item;
    }

}