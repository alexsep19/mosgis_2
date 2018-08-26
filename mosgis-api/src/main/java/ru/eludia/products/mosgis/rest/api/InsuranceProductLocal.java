package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;
import ru.eludia.products.mosgis.rest.api.base.FileBackend;

@Local
public interface InsuranceProductLocal extends CRUDBackend, FileBackend {
    
    JsonObject getVocs ();
    JsonObject doAppend (String id, JsonObject p, User user);

}