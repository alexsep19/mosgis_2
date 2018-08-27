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
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import javax.ws.rs.core.SecurityContext;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.MgmtContractLocal;

@Path ("mgmt_contracts")
public class MgmtContracts extends EJBResource <MgmtContractLocal> {
    
    @Context SecurityContext securityContext;
    
    private User getUser () {
        return (User) securityContext.getUserPrincipal ();
    }
    
    private JsonObject getInnerItem (String id) {
        final JsonObject data = back.getItem (id);        
        final JsonObject item = data.getJsonObject ("item");
        if (item == null) throw new InternalServerErrorException ("Wrong data from back.getItem (" + id + "), no item: " + data);
        return item;
    }    

    private String getUserOrg () {

        String userOrg = getUser ().getUuidOrg ();

        if (userOrg == null) {
            logger.warning ("User has no org set, access prohibited");
            throw new ValidationException ("foo", "Доступ запрещён");
        }

        return userOrg;
        
    }    
    
    private void checkOrg (JsonObject item) {

        String itemOrg = item.getString ("uuid_org", null);

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
        
        if (securityContext.isUserInRole ("nsi_20_1")) {
            
            String toBe = getUser ().getUuidOrg ();
            String asIs = p.getJsonObject ("data").getString ("uuid_org", null);
            
            if (!toBe.equals (asIs)) {
                logger.warning ("Security violation: data.uuid_org must be " + toBe + ", received " + asIs);
                throw new ValidationException ("foo", "Доступ запрещён");
            }
            
        }

        return back.select (p, getUser ()); 
        
    }

    @POST
    @Path ("vocs")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs () { 
        return back.getVocs (); 
    }
    
    @POST
    @Path("create") 
    @Produces (APPLICATION_JSON)
    @RolesAllowed ("nsi_20_1")
    public JsonObject doCreate (JsonObject p) {
        getUserOrg ();
        return back.doCreate (p, getUser ());
    }

    @POST
    @Path("{id}/update") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doUpdate (@PathParam ("id") String id, JsonObject p) {
        final JsonObject item = getInnerItem (id);
        checkOrg (item);
        return back.doUpdate (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/delete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doDelete (@PathParam ("id") String id) { 
        final JsonObject item = getInnerItem (id);
        checkOrg (item);
        return back.doDelete (id, getUser ());
    }
    
    @POST
    @Path("{id}/undelete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doUndelete (@PathParam ("id") String id) { 
        final JsonObject item = getInnerItem (id);
        checkOrg (item);
        return back.doUndelete (id, getUser ());
    }
        
    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
        final JsonObject item = back.getItem (id);
        if (!securityContext.isUserInRole ("admin")) checkOrg (item.getJsonObject ("item"));
        return item;
    }
    
    @POST
    @Path("{id}/log") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        final JsonObject item = back.getItem (id);
        if (!securityContext.isUserInRole ("admin")) checkOrg (item.getJsonObject ("item"));
        return back.getLog (id, p, getUser ());
    }
    
}