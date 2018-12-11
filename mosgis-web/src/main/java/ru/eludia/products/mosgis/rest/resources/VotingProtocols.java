package ru.eludia.products.mosgis.rest.resources;

import java.util.logging.Level;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.json.JsonObject;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.misc.EJBResource;
import ru.eludia.products.mosgis.rest.api.VotingProtocolsLocal;

@Path("voting_protocols")
public class VotingProtocols extends EJBResource<VotingProtocolsLocal> {
    
    private String getUserOrg () {

        String userOrg = getUser ().getUuidOrg ();

        if (userOrg == null) {
            logger.warning ("User has no org set, access prohibited");
            throw new ValidationException ("foo", "Доступ запрещён");
        }

        return userOrg;
        
    }
    
    private boolean selectAccessCheck (JsonObject item) {
        
        if (securityContext.isUserInRole ("admin") ||
            securityContext.isUserInRole ("nsi_20_4") ||
            securityContext.isUserInRole ("nsi_20_7"))
            return true;
        
        JsonObject cach = back.getCach (item.getJsonObject ("data").getString ("uuid_house"));
        String userOrg = getUserOrg ();
        if (cach.containsKey ("cach") && userOrg.equals (cach.getJsonObject ("cach").getString("org.uuid"))) {
            
            return securityContext.isUserInRole ("nsi_20_1") ||
                   securityContext.isUserInRole ("nsi_20_19") ||
                   securityContext.isUserInRole ("nsi_20_20") ||
                   securityContext.isUserInRole ("nsi_20_21") ||
                   securityContext.isUserInRole ("nsi_20_22");
            
        }
        
        return true;
        
    }
    
    private boolean getAccessCheck (JsonObject item) {
        
        if (securityContext.isUserInRole ("admin") ||
            securityContext.isUserInRole ("nsi_20_4") ||
            securityContext.isUserInRole ("nsi_20_7"))
            return true;
        
        String itemOktmo = item.getJsonObject ("item").get ("oktmo").toString ();
        String userOrg = getUserOrg ();
        if (item.containsKey ("cach") && userOrg.equals (item.getJsonObject ("cach").getString("org.uuid"))) {
            
            return securityContext.isUserInRole ("nsi_20_1") ||
                   securityContext.isUserInRole ("nsi_20_19") ||
                   securityContext.isUserInRole ("nsi_20_20") ||
                   securityContext.isUserInRole ("nsi_20_21") ||
                   securityContext.isUserInRole ("nsi_20_22");
            
        }
        else
            return securityContext.isUserInRole("nsi_20_8") && securityContext.isUserInRole("oktmo_" + itemOktmo);
    }
    
    private boolean modAccessCheck (JsonObject item) {
        
        if (item.getJsonObject ("item").getInt ("is_deleted") == 1) return false;
        
        if (securityContext.isUserInRole ("admin")) return true;

        String itemOktmo = item.getJsonObject ("item").get ("oktmo").toString ();
        String userOrg = getUserOrg ();
        if (item.containsKey ("cach") && 
            item.getJsonObject ("cach").getInt ("is_own") == 1 && 
            userOrg.equals (item.getJsonObject ("cach").getString("org.uuid"))) {
            
            return securityContext.isUserInRole ("nsi_20_1") ||
                   securityContext.isUserInRole ("nsi_20_19") ||
                   securityContext.isUserInRole ("nsi_20_20") ||
                   securityContext.isUserInRole ("nsi_20_21") ||
                   securityContext.isUserInRole ("nsi_20_22");
            
        }
        else
            return securityContext.isUserInRole("nsi_20_8") && securityContext.isUserInRole("oktmo_" + itemOktmo);
    }
    
    private boolean newAccessCheck (JsonObject item) {

        JsonObject cach = back.getCach (item.getJsonObject ("data").getString ("fiashouseguid"));
        JsonObject oktmo = back.getOktmo (item.getJsonObject ("data").getString ("fiashouseguid"));

        if (securityContext.isUserInRole("admin")) return true;

        String userOrg = getUserOrg ();
        if (cach.containsKey("cach") && 
            cach.getJsonObject ("cach").getInt ("is_own") == 1 &&
            userOrg.equals (cach.getJsonObject ("cach").getString("org.uuid"))) {
            
            return securityContext.isUserInRole ("nsi_20_1") ||
                   securityContext.isUserInRole ("nsi_20_19") ||
                   securityContext.isUserInRole ("nsi_20_20") ||
                   securityContext.isUserInRole ("nsi_20_21") ||
                   securityContext.isUserInRole ("nsi_20_22");
            
        }
        else
            return securityContext.isUserInRole ("nsi_20_8") && securityContext.isUserInRole("oktmo_" + oktmo.getString ("oktmo"));
        
    }
    
    private void checkSelect (JsonObject item) {
        
        if (!selectAccessCheck (item)) throw new ValidationException ("foo", "Доступ запрещен");
        
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
        checkSelect (p);
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
        checkNew (p);
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
