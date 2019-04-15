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
import ru.eludia.products.mosgis.rest.api.CitizenCompensationCategoryLocal;

@Path ("citizen_compensation_categories")
public class CitizenCompensationCategories extends EJBResource <CitizenCompensationCategoryLocal> {

    private void checkGet() {
	if (!securityContext.isUserInRole("admin")
	    && !securityContext.isUserInRole("nsi_20_10")
	) {
	    throw new ValidationException("foo", "Доступ запрещен");
	}
    }

    private void checkPost() {
	if (!securityContext.isUserInRole("nsi_20_10")) {
	    throw new ValidationException("foo", "Доступ запрещен");
	}
    }

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) {
	checkGet();
        return back.select (p, getUser ()); 
    }

    @POST
    @Path ("vocs")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs () {
	checkGet();
        return back.getVocs (); 
    }
    
    @POST
    @Path("create") 
    @Produces (APPLICATION_JSON)
    public JsonObject doCreate (JsonObject p) {
	checkPost();
        return back.doCreate (p, getUser ());
    }

    @POST
    @Path("{id}/update") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doUpdate (@PathParam ("id") String id, JsonObject p) {
	checkPost();
        return back.doUpdate (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/delete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doDelete (@PathParam ("id") String id) { 
	checkPost();
        return back.doDelete (id, getUser ());
    }
    
    @POST
    @Path("{id}/undelete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doUndelete (@PathParam ("id") String id) { 
	checkPost();
        return back.doUndelete (id, getUser ());
    }
        
    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
	checkGet();
        return back.getItem (id, getUser ());
    }
    
    @POST
    @Path("{id}/log") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
	checkGet();
        return back.getLog (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/calculation")
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getCalculation (@PathParam ("id") String id) {
	checkGet();
        return back.getCalculation (id);
    }
    
    @POST
    @Path("{id}/legal_acts")
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLegalActs (@PathParam ("id") String id) {
	checkGet();
        return back.getLegalActs (id);
    }

//    @POST
//    @Path("import")
//    @Produces (APPLICATION_JSON)
//    public JsonObject doImport () {
//	checkPost();
//        return back.doImport (getUser ());
//    }
}