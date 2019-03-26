package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface RcContractLocal extends CRUDBackend {
    JsonObject getVocs   (JsonObject p);
    JsonObject doApprove(String id);
    JsonObject doAlter(String id);
    JsonObject doAnnul(String id, JsonObject p);
    JsonObject doTerminate(String id, JsonObject p);
}