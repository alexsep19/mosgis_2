package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface VocOverhaulWorkTypesLocal extends CRUDBackend {
    
    JsonObject getVocs ();
    JsonObject doImport (User user);
    JsonObject doAlter(String id, JsonObject p, User user);
    
}
