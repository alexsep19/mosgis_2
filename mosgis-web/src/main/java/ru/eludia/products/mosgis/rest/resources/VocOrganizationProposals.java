package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.api.VocOrganizationProposalsLocal;

@Path ("voc_organization_proposals")
public class VocOrganizationProposals extends EJBResource <VocOrganizationProposalsLocal> {

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
    @Path("vocs")
    @Produces(APPLICATION_JSON)
    public JsonObject getVocs() {
        return back.getVocs();
    }

    @POST
    @Path("create")
    @Produces(APPLICATION_JSON)
    public JsonObject doCreate(JsonObject p) {
        return back.doCreate(p, getUser());
    }

    @POST
    @Path("{id}/update")
    @Produces(APPLICATION_JSON)
    public JsonObject doUpdate(@PathParam("id") String id, JsonObject p) {
        return back.doUpdate(id, p, getUser());
    }

    @POST
    @Path("{id}/delete")
    @Produces(APPLICATION_JSON)
    public JsonObject doDelete(@PathParam("id") String id) {
        return back.doDelete(id, getUser());
    }

    @POST
    @Path("{id}/log")
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        return back.getLog (id, p, getUser ());
    }
}