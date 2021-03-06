package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.SettlementDocLocal;

@Path ("settlement_docs")
public class SettlementDocs extends EJBResource <SettlementDocLocal> {

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) {
        return back.select (p, getUser ());
    }

    @POST
    @Path("create")
    @Produces (APPLICATION_JSON)
    public JsonObject doCreate (JsonObject p) {
        return back.doCreate (p, getUser ());
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
    @Path("{id}/undelete")
    @Produces (APPLICATION_JSON)
    public JsonObject doUndelete (@PathParam ("id") String id) {
        return back.doUndelete (id, getUser ());
    }

    @POST
    @Path("{id}")
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) {
        return back.getItem(id, getUser ());
    }

    @POST
    @Path("{id}/log")
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        return back.getLog (id, p, getUser ());
    }

    @POST
    @Path("vocs")
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs (JsonObject p) {
        return back.getVocs (p);
    }

    @POST
    @Path("{id}/approve")
    @Produces(APPLICATION_JSON)
    public JsonObject doApprove(@PathParam("id") String id) {
	return back.doApprove(id, getUser());
    }

    @POST
    @Path("{id}/alter")
    @Produces(APPLICATION_JSON)
    public JsonObject doAlter(@PathParam("id") String id, JsonObject p) {
	return back.doAlter(id, p, getUser());
    }

    @POST
    @Path("{id}/annul")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public JsonObject doAnnul(@PathParam("id") String id, JsonObject p) {
	return back.doAnnul(id, p, getUser());
    }
}