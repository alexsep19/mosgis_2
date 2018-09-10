package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.api.LivingRoomsLocal;

@Path ("living_rooms")
public class LivingRooms extends EJBResource <LivingRoomsLocal> {
    
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

    Integer [] vocPassportFields = {
        20130,
        20056,	

        20131	
/*
20132	Основание признания комнаты непригодной для проживания
20133	Дата документа, содержащего решение о признании комнаты непригодной для проживания
20134	Номер документа, содержащего решение о признании комнаты непригодной для проживания
20821	Документ, содержащий решение о признании комнаты непригодной для проживания
*/
    };

    @POST
    @Path("{id}/passport_fields")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocPassportFieldsSystems (@PathParam ("id") String id) {
        return back.getVocPassportFields (id, vocPassportFields);
    }

    @POST
    @Path("{id}/set_multiple") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doSetMultiple (@PathParam ("id") String id, JsonObject p) {
        return back.doSetMultiple (id, p);
    }

}