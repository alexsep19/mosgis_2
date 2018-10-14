package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.api.VocOrganizationsLocal;

@Path ("voc_organizations")
public class VocOrganizations extends EJBResource <VocOrganizationsLocal> {

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        return back.select (p); 
    }
    
    @POST
    @Path("import") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doImport (JsonObject p) { 
        return back.doImport (p, getUser ()); 
    }
    
    @POST
    @Path("vocs") 
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs () { 
        return back.getVocs (); 
    }        

    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
        return back.getItem (id);
    }
    
    @POST
    @Path("{id}/mgmt_nsi_58") 
    @Produces (APPLICATION_JSON)
    public JsonObject getMgmtNsi58 (@PathParam ("id") String id) { 
        return back.getMgmtNsi58 (id);
    }
    
    @POST
    @Path("{id}/refresh") 
    @Produces (APPLICATION_JSON)
    public JsonObject doRefresh (@PathParam ("id") String id) { 
        return back.doRefresh (id, getUser ());
    }
    
    @POST
    @Path("{id}/log") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        return back.getLog (id, p, getUser ());
    }
    
}