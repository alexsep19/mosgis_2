package ru.eludia.products.mosgis.rest.resources;

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
import ru.eludia.products.mosgis.rest.api.CharterLocal;

@Path ("charters")
public class Charters extends EJBResource <CharterLocal> {
   
    private JsonObject getInnerItem (String id) {
        final JsonObject data = back.getItem (id, getUser ());        
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
        
        if (securityContext.isUserInRole ("admin")) return;
        if (securityContext.isUserInRole ("nsi_20_4")) return;

        String itemOrg = item.getString ("uuid_org", null);

        if (itemOrg == null) throw new InternalServerErrorException ("Wrong Charter, no org: " + item);

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
/*        
        if (securityContext.isUserInRole ("nsi_20_1")) {
            
            String toBe = getUser ().getUuidOrg ();
            String asIs = p.getJsonObject ("data").getString ("uuid_org", null);
            
            if (!toBe.equals (asIs)) {
                logger.warning ("Security violation: data.uuid_org must be " + toBe + ", received " + asIs);
                throw new ValidationException ("foo", "Доступ запрещён");
            }
            
        }

        return back.select (p, getUser ()); 
*/        

        return null;
        
    }

    @POST
    @Path ("vocs")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs () { 
        return back.getVocs (); 
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
    @Path("{id}/terminate") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doTerminate (@PathParam ("id") String id, JsonObject p) {
        final JsonObject item = getInnerItem (id);
        checkOrg (item);
        return back.doTerminate (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/annul") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doAnnul (@PathParam ("id") String id, JsonObject p) {
        final JsonObject item = getInnerItem (id);
        checkOrg (item);
        return back.doAnnul (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/rollover") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doRollover (@PathParam ("id") String id, JsonObject p) {
        final JsonObject item = getInnerItem (id);
        checkOrg (item);
        return back.doRollover (id, p, getUser ());
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
    @Path("{id}/approve") 
    @Produces (APPLICATION_JSON)
    public JsonObject doApprove (@PathParam ("id") String id) { 
        final JsonObject item = getInnerItem (id);
        checkOrg (item);
        return back.doApprove (id, getUser ());
    }
    
    @POST
    @Path("{id}/alter") 
    @Produces (APPLICATION_JSON)
    public JsonObject doAlter (@PathParam ("id") String id) { 
        final JsonObject item = getInnerItem (id);
        checkOrg (item);
        return back.doAlter (id, getUser ());
    }
    
    @POST
    @Path("{id}/refresh") 
    @Produces (APPLICATION_JSON)
    public JsonObject doRefresh (@PathParam ("id") String id) { 
        final JsonObject item = getInnerItem (id);
        checkOrg (item);
        return back.doRefresh (id, getUser ());
    }    
        
    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
        final JsonObject item = back.getItem (id, getUser ());
        if (!securityContext.isUserInRole ("nsi_20_4") && !securityContext.isUserInRole ("nsi_20_7")) checkOrg (item.getJsonObject ("item"));
        return item;
    }
    
    @POST
    @Path("{id}/log") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        final JsonObject item = back.getItem (id, getUser ());
        if (!securityContext.isUserInRole ("nsi_20_4") && !securityContext.isUserInRole ("nsi_20_7")) checkOrg (item.getJsonObject ("item"));
        return back.getLog (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/reload") 
    @Produces (APPLICATION_JSON)
    public JsonObject doReload (@PathParam ("id") String id) { 
        final JsonObject item = getInnerItem (id);
        checkOrg (item);
        return back.doReload (id, getUser ());
    }

}