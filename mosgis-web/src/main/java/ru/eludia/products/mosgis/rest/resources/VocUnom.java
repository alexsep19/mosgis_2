package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.api.VocUnomLocal;

@Path ("voc_unom")
public class VocUnom extends EJBResource <VocUnomLocal> {

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        return back.select (p);
    }
    
    @POST
    @Path("vocs") 
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs () { 
        return back.getVocs (); 
    }        

}