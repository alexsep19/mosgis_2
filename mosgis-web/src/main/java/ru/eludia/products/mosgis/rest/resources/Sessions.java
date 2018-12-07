package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.api.SessionsLocal;

@Path ("sessions")
public class Sessions extends EJBResource <SessionsLocal> {

    @Context
    private HttpServletRequest request;    

    @POST
    @Path ("create")
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject create (JsonObject p, @HeaderParam ("X-Request-Param-password") String password) { 
        
        final JsonObject user = back.create (p.getJsonObject ("data").getString ("login"), password);
        
        if (user != null) {
            
            HttpSession session = request.getSession (true);
            request.changeSessionId ();

            session.setAttribute ("user.id", user.getString ("id"));
            session.setAttribute ("user.label", user.getString ("label"));
            session.setAttribute ("user.uuid_org", user.getString ("uuid_org", null));
            session.setAttribute ("user.roles", user.getJsonObject ("role").keySet ().toArray ());
            session.setAttribute ("user.oktmo", user.getJsonObject ("oktmo").keySet ().toArray ());
            
            return Json.createObjectBuilder ()
                .add ("user", user)
                .add ("timeout", session.getMaxInactiveInterval () / 60)
            .build ();

        }
        else {
            return Json.createObjectBuilder ().build ();
        }
                
    }

}