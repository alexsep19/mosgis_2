package ru.eludia.products.mosgis.rest.misc;

import ru.eludia.products.mosgis.rest.User;
import java.security.Principal;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.core.SecurityContext;

public class SecurityCtx implements SecurityContext {
    
    User user;
    Object [] roles;
    Object [] oktmo;

    public SecurityCtx (String id, String name, String uuid_org, Object [] roles, Object [] oktmo) {
        user = new User (id, name, uuid_org);
        this.roles = roles;
        this.oktmo = oktmo;
    }

    @Override
    public Principal getUserPrincipal () {
        return user;
    }
    
    public boolean isOktmoIn (String oktmo) {
        for (Object i: roles) if (oktmo.equals(i)) return true;
        return false;
    }

    @Override
    public boolean isUserInRole (String role) {
        for (Object i: roles) if (role.equals (i)) return true;
        return false;
    }

    @Override
    public boolean isSecure () {
        return true;
    }

    @Override
    public String getAuthenticationScheme () {
        return FORM_AUTH;
    }

    @Override
    public String toString () {
        return toJsonObject ().toString ();
    }
    
    public JsonObject toJsonObject () {
        JsonArrayBuilder ab = Json.createArrayBuilder ();
        for (Object i: roles) ab.add (i.toString ());
        return Json.createObjectBuilder ().add ("user", user.toJsonObject ()).add ("roles", ab).build ();
    }
    
}
