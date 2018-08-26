package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.api.VocNsiListLocal;

@Path ("voc_nsi_list")
public class VocNsiList extends EJBResource <VocNsiListLocal> {

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        return back.select (p); 
    }

    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
        return back.getItem (id);
    }

    @POST
    @Path("{id}/import") 
    @Produces (APPLICATION_JSON)
    public JsonObject doImport (@PathParam ("id") String id) { 
        return back.doImport (id);
    }

    @POST
    @Path("{id}/lines") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLines (@PathParam ("id") String id, JsonObject p) { 
        return back.getLines (id, p);
    }

    @POST
    @Path("vocs") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs (JsonObject p) { 
        return back.getVocs (p);
    }

}