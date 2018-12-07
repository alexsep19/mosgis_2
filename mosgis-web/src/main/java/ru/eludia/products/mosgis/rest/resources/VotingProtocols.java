package ru.eludia.products.mosgis.rest.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.json.JsonObject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import javax.ws.rs.core.SecurityContext;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.misc.EJBResource;
import ru.eludia.products.mosgis.rest.api.VotingProtocolsLocal;
import ru.eludia.products.mosgis.rest.misc.SecurityCtx;

@Path("voting_protocols")
public class VotingProtocols extends EJBResource<VotingProtocolsLocal> {

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
             securityContext.isUserInRole ("nsi_20_22")) && userOrg.equals (itemOrg) && item.containsKey("cach"))
            return true;
        
        if (securityContext.isUserInRole("nsi_20_8")) {
            String itemOktmo = item.getJsonObject ("item").get ("oktmo").toString ();
            Object [] oktmos = (Object []) httpServletRequest.getSession (false).getAttribute("user.oktmo");
            for (int i = 0; i < oktmos.length; i++)
                if (itemOktmo.equals (String.valueOf (oktmos[i])))
                    return true;
        }
        
        return false;
    }
    
    private void checkGet (JsonObject item) {
        
        if (!getAccessCheck (item)) throw new ValidationException ("foo", "Доступ запрещен");
        
    }
    
    private void checkOrg (JsonObject item) {

        String itemOrg = item.getString ("uuid_org", null);

        if (itemOrg == null) throw new InternalServerErrorException ("Wrong voting protocol, no org: " + item);

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
