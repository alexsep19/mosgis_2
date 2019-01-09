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
import ru.eludia.products.mosgis.rest.api.WorkingPlanLocal;

@Path ("working_plans")
public class WorkingPlans extends EJBResource <WorkingPlanLocal> {
/*    
    private JsonObject getData (String id) {
        return back.getItem (id);
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
*/

    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
        final JsonObject item = back.getItem (id);
        return item;
    }

    @POST
    @Path("{id}/update") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doUpdate (@PathParam ("id") String id, JsonObject p) {
//        final JsonObject item = getData (id);
//        checkOrg (item);
        return back.doUpdate (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/log") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
//        final JsonObject item = back.getItem (id);
//        if (!securityContext.isUserInRole ("admin")) checkOrg (item);
        return back.getLog (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/approve") 
    @Produces (APPLICATION_JSON)
    public JsonObject doApprove (@PathParam ("id") String id) { 
        return back.doApprove (id, getUser ());
    }

}