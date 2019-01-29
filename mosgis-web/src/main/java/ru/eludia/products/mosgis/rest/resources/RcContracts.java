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
import ru.eludia.products.mosgis.rest.api.RcContractLocal;

@Path ("rc_contracts")
public class RcContracts extends EJBResource <RcContractLocal> {

    private boolean isGlobalUser () {

        if (securityContext.isUserInRole ("admin")
            || securityContext.isUserInRole("nsi_20_7")
            || securityContext.isUserInRole("nsi_20_8")
        ) {
            return true;
        }
        return false;
    }

    private JsonObject getInnerItem (String id) {
        final JsonObject data = back.getItem (id);
        final JsonObject item = data.getJsonObject ("item");
        if (item == null) throw new InternalServerErrorException ("Wrong data from back.getItem (" + id + "), no item: " + data);
        return item;
    }

    private String getUserOrg () {

        String userOrg = getUser ().getUuidOrg ();

        if (userOrg == null) {
            logger.warning ("User has no org set, access prohibited");
            throw new ValidationException ("foo", "Доступ запрещён");
        }

        return userOrg;

    }

    private void checkOrg (JsonObject item) {
    }

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) {

        final JsonObject data = p.getJsonObject ("data");

        if (!isGlobalUser ()
            && !getUserOrg().equals (data.getString ("uuid_org", null))
            && !getUserOrg().equals(data.getString("uuid_org_customer", null))
        ) {
            throw new ValidationException ("foo", "Доступ запрещён");
        }

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
        final JsonObject item = getInnerItem (id);
        checkOrg (item);
        return back.doUpdate (id, p, getUser ());
    }

    @POST
    @Path("{id}/delete")
    @Produces (APPLICATION_JSON)
    public JsonObject doDelete (@PathParam ("id") String id) {
        final JsonObject item = getInnerItem (id);
        checkOrg (item);
        return back.doDelete (id, getUser ());
    }

    @POST
    @Path("{id}/undelete")
    @Produces (APPLICATION_JSON)
    public JsonObject doUndelete (@PathParam ("id") String id) {
        final JsonObject item = getInnerItem (id);
        checkOrg (item);
        return back.doUndelete (id, getUser ());
    }

    @POST
    @Path("{id}")
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) {
        final JsonObject item = back.getItem (id);
        if (!securityContext.isUserInRole ("admin")) checkOrg (item.getJsonObject ("item"));
        return item;
    }

    @POST
    @Path("{id}/log")
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        final JsonObject item = back.getItem (id);
        if (!securityContext.isUserInRole ("admin")) checkOrg (item.getJsonObject ("item"));
        return back.getLog (id, p, getUser ());
    }

    @POST
    @Path("vocs")
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs (JsonObject p) {
        return back.getVocs (p);
    }
}