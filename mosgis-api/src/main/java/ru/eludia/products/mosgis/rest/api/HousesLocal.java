package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface HousesLocal extends CRUDBackend {
    
    JsonObject doPatch(String id, JsonObject p);

    JsonObject getVocPassportFields(String id, Integer[] ids);

    JsonObject doSetMultiple(String id, JsonObject p);

    JsonObject doReload(String id, String uuidOrg, User user);

    JsonObject doSend(String id, String uuidOrg, User user);
}