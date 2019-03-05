package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.api.VocBicLocal;

@Path ("voc_bic")
public class VocBic extends EJBResource <VocBicLocal> {

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        return back.select (p); 
    }

    @POST
    @Path("{id}/import") 
    @Produces (APPLICATION_JSON)
    public JsonObject doImport (@PathParam ("id") String id) { 
        return back.doImport (getUser ());
    }

    @POST
    @Path("vocs") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs (JsonObject p) { 
        return back.getVocs (p);
    }

}