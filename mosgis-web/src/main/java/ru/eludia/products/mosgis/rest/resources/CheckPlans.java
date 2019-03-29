package ru.eludia.products.mosgis.rest.resources;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.CheckPlansLocal;
import ru.eludia.products.mosgis.rest.misc.EJBResource;

@Path("check_plans")
public class CheckPlans extends EJBResource <CheckPlansLocal> {

	private void check() {

		if (!securityContext.isUserInRole("nsi_20_4") 
				&& !securityContext.isUserInRole("nsi_20_5")
				&& !securityContext.isUserInRole("admin"))
			throw new ValidationException("foo", "Доступ запрещён");
	}
	
	private void checkCreate() {

		if (!securityContext.isUserInRole("nsi_20_4") 
				&& !securityContext.isUserInRole("nsi_20_5"))
			throw new ValidationException("foo", "Доступ запрещён");
	}
    
    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        check ();
        return back.select (p, getUser ()); 
    }
    
	@POST
	@Path("create")
	@Produces(APPLICATION_JSON)
	public JsonObject doCreate(JsonObject p) {
		checkCreate();
		return back.doCreate(p, getUser());
	}
    
    @POST
    @Path("{id}/delete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doDelete (@PathParam ("id") String id) {
        check ();
        return back.doDelete (id, getUser ());
    }
    
    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) {
        check ();
        return back.getItem (id, getUser ());
    }
    
    @POST
    @Path("{id}/log") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        check ();
        return back.getLog (id, p, getUser ());
    }
    
    @POST
    @Path ("vocs")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs () { 
        check ();
        return back.getVocs (); 
    }
    
    @POST
    @Path("{id}/update") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doUpdate (@PathParam ("id") String id, JsonObject p) {
        check ();
        return back.doUpdate (id, p, getUser ());
    }
    
    @POST
    @Path("import") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doImport (JsonObject p) {
        check ();
        return back.doImport (p, getUser ());
    }
    
	@POST
	@Path("{id}/send")
	@Produces(APPLICATION_JSON)
	public JsonObject doApprove(@PathParam("id") String id) {
		check();
		return back.doSend(id, getUser());
	}
}
