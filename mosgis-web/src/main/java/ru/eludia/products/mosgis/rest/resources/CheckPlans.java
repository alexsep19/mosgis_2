package ru.eludia.products.mosgis.rest.resources;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.api.CheckPlansLocal;
import ru.eludia.products.mosgis.rest.misc.EJBResource;

@Path("check_plans")
public class CheckPlans extends EJBResource <CheckPlansLocal> {

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        return back.select (p, getUser ()); 
    }
    
}
