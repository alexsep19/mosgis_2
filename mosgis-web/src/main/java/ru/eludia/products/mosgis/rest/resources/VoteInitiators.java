package ru.eludia.products.mosgis.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.json.JsonObject;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.VoteInitiatorsLocal;
import ru.eludia.products.mosgis.rest.misc.EJBResource;

@Path("vote_initiators")
public class VoteInitiators extends EJBResource<VoteInitiatorsLocal> {

    private String getUserOrg () {

        String userOrg = getUser ().getUuidOrg ();

        if (userOrg == null) {
            logger.warning ("User has no org set, access prohibited");
            throw new ValidationException ("foo", "Доступ запрещён");
        }

        return userOrg;
        
    }
    
    private boolean getAccessCheck (JsonObject item) {
        
        if (securityContext.isUserInRole ("admin") ||
            securityContext.isUserInRole ("nsi_20_4") ||
            securityContext.isUserInRole ("nsi_20_7"))
            return true;
        
        String itemOrg = item.getJsonObject ("item").getString ("uuid_org");
        String userOrg = getUserOrg ();
        if ((securityContext.isUserInRole ("nsi_20_1")  ||
             securityContext.isUserInRole ("nsi_20_19") ||
             securityContext.isUserInRole ("nsi_20_20") ||
             securityContext.isUserInRole ("nsi_20_21") || 
             securityContext.isUserInRole ("nsi_20_22")) && userOrg.equals (itemOrg))
            return true;
        
        if (securityContext.isUserInRole("nsi_20_8")) {
            String itemOktmo = item.getJsonObject ("item").get ("oktmo").toString ();
            if (securityContext.isUserInRole("oktmo_" + itemOktmo)) return true;
        }
        
        return false;
    }
    
    private boolean modAccessCheck (JsonObject item) {
        
        if ("1".equals (item.getJsonObject ("item").get ("is_deleted").toString ())) return false;
        
        if (securityContext.isUserInRole ("admin")) return true;
        
        String itemOrg = item.getJsonObject ("item").getString ("uuid_org");
        String userOrg = getUserOrg ();
        if (securityContext.isUserInRole ("nsi_20_1") ||
            securityContext.isUserInRole ("nsi_20_19") ||
            securityContext.isUserInRole ("nsi_20_20") ||
            securityContext.isUserInRole ("nsi_20_21") ||
            securityContext.isUserInRole ("nsi_20_22") && 
            userOrg.equals (itemOrg) &&
            item.containsKey ("cach") && 
            "1".equals (item.getJsonObject ("cach").get ("is_own").toString ()))
            return true;
        
        if (securityContext.isUserInRole("nsi_20_8")) {
            String itemOktmo = item.getJsonObject ("item").get ("oktmo").toString ();
            if (securityContext.isUserInRole("oktmo_" + itemOktmo)) return true;
        }
        
        return false;
    }
    
    private boolean newAccessCheck (JsonObject item) {

        JsonObject cach = back.getCach(item.getJsonObject ("data").getString ("fiashouseguid"));
        JsonObject oktmo = back.getOktmo(item.getJsonObject ("data").getString ("fiashouseguid"));

        if (securityContext.isUserInRole("admin")) return true;

        if (securityContext.isUserInRole ("nsi_20_1") ||
            securityContext.isUserInRole ("nsi_20_19") ||
            securityContext.isUserInRole ("nsi_20_20") ||
            securityContext.isUserInRole ("nsi_20_21") ||
            securityContext.isUserInRole ("nsi_20_22") && 
                cach.containsKey ("cach") && 
                "1".equals (cach.getJsonObject ("cach").getString ("is_own")))
            return true;

        return securityContext.isUserInRole ("nsi_20_8") && securityContext.isUserInRole("oktmo_" + oktmo.getString ("oktmo"));
        
    }
    
    private void checkGet (JsonObject item) {
        
        if (!getAccessCheck (item)) throw new ValidationException ("foo", "Доступ запрещен");
        
    }
    
    private void checkMod (JsonObject item) {
        
        if (!modAccessCheck (item)) throw new ValidationException ("foo", "Доступ запрещен");
        
    }
    
    private void checkNew (JsonObject item) {
        
        if (!newAccessCheck (item)) throw new ValidationException ("foo", "Доступ запрещен");
        
    }
    
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
        getUserOrg ();
        return back.doCreate (p, getUser ());
    }

    @POST
    @Path("{id}/update") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doUpdate (@PathParam ("id") String id, JsonObject p) {
        final JsonObject item = back.getItem (id);
        checkMod (item);
        return back.doUpdate (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/delete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doDelete (@PathParam ("id") String id) { 
        final JsonObject item = back.getItem (id);
        checkMod (item);
        return back.doDelete (id, getUser ());
    }
    
    @POST
    @Path("{id}/undelete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doUndelete (@PathParam ("id") String id) { 
        final JsonObject item = back.getItem (id);
        checkMod (item);
        return back.doUndelete (id, getUser ());
    }
        
    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
        final JsonObject item = back.getItem (id);
        checkGet (item);
        return item;
    }
    
    @POST
    @Path("{id}/log") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        final JsonObject item = back.getItem (id);
        checkGet (item);
        return back.getLog (id, p, getUser ());
    }
}
