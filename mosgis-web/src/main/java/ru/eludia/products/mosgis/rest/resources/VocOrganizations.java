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
import ru.eludia.products.mosgis.rest.api.VocOrganizationsLocal;

@Path ("voc_organizations")
public class VocOrganizations extends EJBResource <VocOrganizationsLocal> {

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        return back.select (p, getUser ()); 
    }
    
    @POST
    @Path("list")
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject list (JsonObject p) {
        return back.list (p);
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
    @Path("{id}/hours")
    @Produces(APPLICATION_JSON)
    public JsonObject getHours(@PathParam("id") String id) {
        return back.getHours(id);
    }

    @POST
    @Path("{id}/patch_hours")
    @Produces(APPLICATION_JSON)
    public JsonObject doPatchHours(@PathParam("id") String id, JsonObject p) {
        return back.doPatchHours(id, p);
    }

    @POST
    @Path("{id}/refresh") 
    @Produces (APPLICATION_JSON)
    public JsonObject doRefresh (@PathParam ("id") String id) { 
        return back.doRefresh (id, getUser ());
    }

    @POST
    @Path("{id}/patch")
    @Produces(APPLICATION_JSON)
    public JsonObject doPatch(@PathParam("id") String id, JsonObject p) {
        return back.doPatch(id, p, getUser());
    }

    @POST
    @Path("{id}/log") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        return back.getLog (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/import_mgmt_contracts") 
    @Produces (APPLICATION_JSON)
    @RolesAllowed ("admin")
    public JsonObject doImportMgmtContracts (@PathParam ("id") String id) { 
        return back.doImportMgmtContracts (id, getUser ());
    }
    
}