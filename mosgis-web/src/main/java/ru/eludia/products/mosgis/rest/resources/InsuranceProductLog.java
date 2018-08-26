package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.InsuranceProductLogLocal;

@Path ("insurance_product_log")
public class InsuranceProductLog extends EJBResource <InsuranceProductLogLocal> {
    
    @Context SecurityContext securityContext;
    
//    @EJB
//    InsuranceProductLocal insuranceProductLocal;
    
    private User getUser () {
        return (User) securityContext.getUserPrincipal ();
    }

    private String getUserOrg () {

        String userOrg = getUser ().getUuidOrg ();

        if (userOrg == null) {
            logger.warning ("User has no org set, access prohibited");
            throw new ValidationException ("foo", "Доступ запрещён");
        }

        return userOrg;
        
    }    
    
    private void checkOrg (String itemOrg) {
   
        if (itemOrg == null) throw new InternalServerErrorException ("Wrong data: no org");

        String userOrg = getUserOrg ();

        if (!userOrg.equals (itemOrg)) {
            logger.warning ("Org mismatch: " + userOrg + " vs. " + itemOrg);
            throw new ValidationException ("foo", "Доступ запрещён");
        }

    }

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 

/*        
        if (!securityContext.isUserInRole ("admin")) {
            JsonObject item = insuranceProductLocal.getItem (p.getJsonObject ("data").getString ("uuid_object"));
            checkOrg (item.getString ("uuid_org", null));
        }
*/
        return back.select (p, getUser ());

    }    
    
    @POST
    @Path("{id}/download") 
    @Produces(APPLICATION_OCTET_STREAM)
    public Response download (@PathParam ("id") String id) {

        JsonObject item = back.getItem (id).getJsonObject ("item");
        if (!securityContext.isUserInRole ("admin")) checkOrg (item.getString ("p.uuid_org", null));
        return createFileDownloadResponse (id, item.getString ("name"), item.getInt ("len"));

    }
    
}