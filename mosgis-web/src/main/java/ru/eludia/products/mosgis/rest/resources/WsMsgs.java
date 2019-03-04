package ru.eludia.products.mosgis.rest.resources;

import javax.annotation.security.RolesAllowed;
import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.api.WsMsgLocal;

@Path ("ws_msgs")
@RolesAllowed ("admin")
public class WsMsgs extends EJBResource <WsMsgLocal> {

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        return back.select (p);
    }
    
    @POST
    @Path ("vocs")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs () {
        return back.getVocs ();
    }
    
    @POST
    @Path("{id}/rq") 
    @Produces (APPLICATION_JSON)
    public JsonObject getRq (@PathParam ("id") String id) { 
        return back.getRq (id);
    }
    
    @POST
    @Path("{id}/rp") 
    @Produces (APPLICATION_JSON)
    public JsonObject getRp (@PathParam ("id") String id) { 
        return back.getRp (id);
    }

}