package ru.eludia.products.mosgis.rest.api.base;

import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;

public interface CRUDBackend {
    
    JsonObject select     (JsonObject p, User user);
    JsonObject getItem    (String id);
    JsonObject doCreate   (JsonObject p, User user);
    JsonObject doUpdate   (String id, JsonObject p, User user);
    JsonObject doDelete   (String id, User user);
    JsonObject doUndelete (String id, User user);
    JsonObject getLog     (String id, JsonObject p, User user);

}