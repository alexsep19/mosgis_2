package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.WorkingListLocal;

@Path ("working_lists")
public class WorkingLists extends EJBResource <WorkingListLocal> {
    
    private JsonObject getData (String id) {
        return back.getItem (id, getUser ());
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

        if (securityContext.isUserInRole ("admin")) return;
        
        if (!(
            securityContext.isUserInRole ("nsi_20_1")
            || securityContext.isUserInRole ("nsi_20_19")
            || securityContext.isUserInRole ("nsi_20_20")
            || securityContext.isUserInRole ("nsi_20_21")
            || securityContext.isUserInRole ("nsi_20_22")
        )) throw new ValidationException ("foo", "Доступ запрещён");

        if (!item.containsKey ("cach") || !item.getJsonObject ("cach").getString ("org.uuid").equals (getUser ().getUuidOrg ())) throw new ValidationException ("foo", "Ваша организация не управляет домом по этому адресу. Доступ запрещён.");

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
        getUserOrg ();
        return back.doCreate (p, getUser ());
    }

    @POST
    @Path("{id}/update") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doUpdate (@PathParam ("id") String id, JsonObject p) {
        final JsonObject item = getData (id);
        checkOrg (item);
        return back.doUpdate (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/delete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doDelete (@PathParam ("id") String id) { 
        final JsonObject item = getData (id);
        checkOrg (item);
        return back.doDelete (id, getUser ());
    }
            
    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
        final JsonObject item = back.getItem (id, getUser ());
        return item;
    }
    
    @POST
    @Path("{id}/log") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        final JsonObject item = back.getItem (id, getUser ());
        if (!securityContext.isUserInRole ("admin")) checkOrg (item);
        return back.getLog (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/add_items") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doAddItems (@PathParam ("id") String id, JsonObject p) {
        final JsonObject item = getData (id);
        checkOrg (item);
        return back.doAddItems (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/approve") 
    @Produces (APPLICATION_JSON)
    public JsonObject doApprove (@PathParam ("id") String id) { 
        return back.doApprove (id, getUser ());
    }
    
    @POST
    @Path("{id}/cancel") 
    @Produces (APPLICATION_JSON)
    public JsonObject doCancel (@PathParam ("id") String id) { 
        return back.doCancel (id, getUser ());
    }

    @POST
    @Path("{id}/alter") 
    @Produces (APPLICATION_JSON)
    public JsonObject doAlter (@PathParam ("id") String id, JsonObject p) { 
        return back.doAlter (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/annul")
    @Produces(APPLICATION_JSON)
    public JsonObject doAnnul(@PathParam("id") String id, JsonObject p) {
	return back.doAnnul(id, p, getUser());
    }    

}