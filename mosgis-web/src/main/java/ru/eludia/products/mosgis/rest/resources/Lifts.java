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
import ru.eludia.products.mosgis.rest.api.LiftsLocal;

@Path ("lifts")
public class Lifts extends EJBResource <LiftsLocal> {

    private void createCheck (JsonObject p) {
        
        final JsonObject data = p.getJsonObject ("data");
        final String uuid_house = data.getString ("uuid_house");
        
        if (!back.checkCreate(uuid_house, p.getString("factorynum")))
            throw new ValidationException ("foo", "Указан недопустимый заводской номер лифта");
        
    }
    
    private void restoreCheck (String id) {
        
        if (!back.checkRestore(id))
            throw new ValidationException ("foo", "Невозможно восстановить запись");
        
    }
    
    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        return back.select (p, getUser ()); 
    }

    @POST
    @Path("create") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject create (JsonObject p) { 
        return back.doCreate (p, getUser ()); 
    }
    
    @POST
    @Path("add") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doAdd (JsonObject p) { 
        return back.doAdd (p); 
    }

    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
        return back.getItem (id);
    }
        
    @POST
    @Path("{id}/update") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doUpdate (@PathParam ("id") String id, JsonObject p) {
        return back.doUpdate (id, p, getUser ());
    }
    
    @POST
    @Path("{id}/delete") 
    @Produces (APPLICATION_JSON)
    public JsonObject doDelete (@PathParam ("id") String id) { 
        return back.doDelete (id, getUser ());
    }

    @POST
    @Path ("{id}/restore")
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doRestore (@PathParam ("id") String id, JsonObject p) {
        restoreCheck (id);
        return back.doRestore(id, p);
    }
    
}