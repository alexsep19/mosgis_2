package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface AcknowledgmentLocal extends CRUDBackend {

    JsonObject doPatch (String id, JsonObject p, User user);
    JsonObject doDistribute (String id, JsonObject p, User user);
    JsonObject doApprove    (String id, User user);
    JsonObject doAlter      (String id, User user);

}