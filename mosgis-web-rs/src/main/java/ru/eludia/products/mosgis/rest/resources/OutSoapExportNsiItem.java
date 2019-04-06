package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.api.OutSoapExportNsiItemLocal;

@Path ("out_soap_export_nsi_item")
public class OutSoapExportNsiItem extends EJBResource <OutSoapExportNsiItemLocal> {

    @POST
    @Path("stats") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getStats (JsonObject p) { 
        return back.getStats (p); 
    }

    @POST
    @Path("{id}/rq") 
    @Produces (APPLICATION_JSON)
    public JsonObject getRq (@PathParam ("id") String id) { 
        return back.getRq (id);
    }
    
    @POST
    @Path("{id}/rp") 
    @Produces (APPLICATION_JSON)
    public JsonObject getRp (@PathParam ("id") String id) { 
        return back.getRp (id);
    }
    
    @POST
    @Path("{dt}/errors") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getErrors (@PathParam ("dt") String dt, JsonObject p) { 
        return back.getErrors (dt, p); 
    }

}