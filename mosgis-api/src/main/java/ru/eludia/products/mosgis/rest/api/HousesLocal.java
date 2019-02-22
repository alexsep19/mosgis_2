package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface HousesLocal extends CRUDBackend {

    JsonObject selectAll (JsonObject p);
    JsonObject selectOktmo (JsonObject p, String uuid_org);
    
    JsonObject select (JsonObject p, User user);
    JsonObject doUpdate (String id, JsonObject p);
    JsonObject doPatch  (String id, JsonObject p);
    JsonObject getVocs ();
    JsonObject getVocPassportFields (String id, Integer[] ids);
    JsonObject doSetMultiple (String id, JsonObject p);
    JsonObject doCreate (JsonObject p, User user);

    JsonObject doReload(String id, String uuidOrg, User user);
    JsonObject doSend(String id, String uuidOrg, User user);
}