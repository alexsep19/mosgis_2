package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.api.VocDifferentiationLocal;

@Path ("voc_differentiation")
public class VocDifferentiation extends EJBResource <VocDifferentiationLocal> {

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        return back.select (p); 
    }

    @POST
    @Path("import")
    @Produces (APPLICATION_JSON)
    public JsonObject doImport () {
        return back.doImport (getUser ());
    }

    @POST
    @Path("log") 
    @Produces (APPLICATION_JSON)
    public JsonObject getLog () { 
        return back.getLog (); 
    }

}