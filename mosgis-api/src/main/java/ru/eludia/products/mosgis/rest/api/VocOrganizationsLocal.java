package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface VocOrganizationsLocal extends CRUDBackend {
    
    JsonObject getVocs      ();
    JsonObject select       (JsonObject p);
    JsonObject getItem      (String id);
    JsonObject doImport     (JsonObject p, User user);
    JsonObject getMgmtNsi58 (String id);
    JsonObject getHours   (String id);
    JsonObject doRefresh    (String id, User user);
    JsonObject doPatch      (String id, JsonObject p, User user);
    JsonObject doPatchHours (String id, JsonObject p);
}