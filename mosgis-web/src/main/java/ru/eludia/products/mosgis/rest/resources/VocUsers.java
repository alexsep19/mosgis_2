package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import javax.ws.rs.core.SecurityContext;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.VocUsersLocal;

@Path ("voc_users")
//@RolesAllowed ("admin")
public class VocUsers extends EJBResource <VocUsersLocal> {

    @Context SecurityContext securityContext;
    
    protected boolean isUserAdmin () {
        return securityContext.isUserInRole ("admin");
    }

    private User getUser () {
        return (User) securityContext.getUserPrincipal ();
    }
    
    private void croak () {
        throw new ValidationException ("foo", "Доступ запрещён");
    }
    
    private void checkData (JsonObject p) {
        if (isUserAdmin ()) return;
        final JsonObject data = p.getJsonObject ("data");
        if (data == null) croak ();
        final String uuid = data.getString ("uuid_org", null);
        if (uuid == null || !uuid.equals (getUser ().getUuidOrg ())) croak ();
    }    
    
    private void checkItem (String id) {
        if (isUserAdmin ()) return;
        JsonObject data = back.getItem (id).getJsonObject ("item");
        final String uuid = data.getString ("uuid_org", null);
        if (uuid == null || !uuid.equals (getUser ().getUuidOrg ())) croak ();
    }
    

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        checkData (p);
        return back.select (p);         
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
    public JsonObject doCreate (JsonObject p) {
        checkData (p);
        return back.doCreate (p);
    }

    @POST
    @Path("{id}/update") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doUpdate (@PathParam ("id") String id, JsonObject p) {
        checkItem (id);
        return back.doUpdate (id, p);
    }
    
    @POST
    @Path("{id}/delete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doDelete (@PathParam ("id") String id) { 
        checkItem (id);
        return back.doDelete (id);
    }
    
    @POST
    @Path("{id}/set_password") 
    @Produces (APPLICATION_JSON)    
    public JsonObject doSetPassword (@PathParam ("id") String id, @HeaderParam ("X-Request-Param-password") String password) { 
        checkItem (id);
        return back.doSetPassword (id, password);
    }

}