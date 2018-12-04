package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;

@Local
public interface VocOrganizationTerritoriesLocal {

    public JsonObject select(JsonObject p, User user);
    public JsonObject doCreate(JsonObject p, User user);
    public JsonObject doDelete(String id, User user);
    
}
