package ru.eludia.products.mosgis.rest.resources;

import javax.json.JsonObject;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.OverhaulAddressProgramsLocal;
import ru.eludia.products.mosgis.rest.misc.EJBResource;

@Path("overhaul_address_programs")
public class OverhaulAddressPrograms extends EJBResource <OverhaulAddressProgramsLocal> {
    
    private void checkGet () {
        if (!securityContext.isUserInRole ("admin")    && 
            !securityContext.isUserInRole ("nsi_20_7") &&
            !securityContext.isUserInRole ("nsi_20_14"))
            throw new ValidationException ("foo", "Доступ запрещен");
    }
    
    private void checkPost () {
        if (!securityContext.isUserInRole ("admin") && !securityContext.isUserInRole ("nsi_20_7")) throw new ValidationException ("foo", "Доступ запрещен");
    }
    
    private void checkWorksAndDocuments (String id) {
        JsonObject object = back.getItem (id, getUser ());
        if (!object.containsKey ("works") || object.getJsonArray ("works").isEmpty ()) throw new ValidationException ("foo", "К РПКР должен быть привязан хотя бы один вид работ");
        if (!object.containsKey ("documents") || object.getJsonArray ("documents").isEmpty ()) throw new ValidationException ("foo", "К РПКР должен быть привязан хотя бы один нормативный документ с файлами");
    }
    
    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) {
        checkGet ();
        return back.getItem (id, getUser ());
    }
    
    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        checkGet ();
        return back.select (p, getUser ());
    }
    
    @POST
    @Path("vocs") 
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs () {
        return back.getVocs ();
    }
    
    @POST
    @Path("{id}/log")
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        checkGet ();
        return back.getLog (id, p, getUser ());
    }
    
    @POST
    @Path("create") 
    @Produces (APPLICATION_JSON)
    public JsonObject doCreate (JsonObject p) {
        checkPost ();
        return back.doCreate (p, getUser ());
    }
    
    @POST
    @Path("{id}/update")
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doUpdate (@PathParam ("id") String id, JsonObject p) {
        checkPost ();
        return back.doUpdate (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/delete")
    @Produces (APPLICATION_JSON)
    public JsonObject doDelete (@PathParam ("id") String id) {
        checkPost ();
        return back.doDelete (id, getUser ());
    }
    
    @POST
    @Path("{id}/approve") 
    @Produces (APPLICATION_JSON)
    public JsonObject doApprove (@PathParam ("id") String id) {
        checkWorksAndDocuments (id);
        return back.doApprove (id, getUser ());
    }
    
    @POST
    @Path("{id}/alter") 
    @Produces (APPLICATION_JSON)
    public JsonObject doAlter (@PathParam ("id") String id) { 
        return back.doAlter (id, getUser ());
    }
    
}
