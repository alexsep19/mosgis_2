package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface RcContractObjectLocal extends CRUDBackend {
    JsonObject getVocs      (JsonObject p);
    JsonObject getBuildings (JsonObject p, User u);
}