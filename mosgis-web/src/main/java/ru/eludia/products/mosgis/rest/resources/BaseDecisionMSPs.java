package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.api.BaseDecisionMSPLocal;

@Path ("base_decision_msps")
public class BaseDecisionMSPs extends EJBResource <BaseDecisionMSPLocal> {
/*    
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

        if (itemOrg == null) throw new InternalServerErrorException ("Wrong MSPDecisionBase, no org: " + item);

        String userOrg = getUserOrg ();

        if (!userOrg.equals (itemOrg)) {
            logger.warning ("Org mismatch: " + userOrg + " vs. " + itemOrg);
            throw new ValidationException ("foo", "Доступ запрещён");
        }

    }
*/

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
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
    public JsonObject doCreate (JsonObject p) {
//        getUserOrg ();
        return back.doCreate (p, getUser ());
    }

    @POST
    @Path("{id}/update") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doUpdate (@PathParam ("id") String id, JsonObject p) {
//        final JsonObject item = getInnerItem (id);
//        checkOrg (item);
        return back.doUpdate (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/delete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doDelete (@PathParam ("id") String id) { 
//        final JsonObject item = getInnerItem (id);
//        checkOrg (item);
        return back.doDelete (id, getUser ());
    }
    
    @POST
    @Path("{id}/undelete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doUndelete (@PathParam ("id") String id) { 
//        final JsonObject item = getInnerItem (id);
//        checkOrg (item);
        return back.doUndelete (id, getUser ());
    }
        
    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
        final JsonObject item = back.getItem (id, getUser ());
//        if (!securityContext.isUserInRole ("admin")) checkOrg (item.getJsonObject ("item"));
        return item;
    }
    
    @POST
    @Path("{id}/log") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
//        final JsonObject item = back.getItem (id);
//        if (!securityContext.isUserInRole ("admin")) checkOrg (item.getJsonObject ("item"));
        return back.getLog (id, p, getUser ());
    }

    @POST
    @Path("import")
    @Produces (APPLICATION_JSON)
    public JsonObject doImport () {
        return back.doImport (getUser ());
    }

    @POST
    @Path("log") 
    @Produces (APPLICATION_JSON)
    public JsonObject getLog () { 
        return back.getLog (); 
    }
}