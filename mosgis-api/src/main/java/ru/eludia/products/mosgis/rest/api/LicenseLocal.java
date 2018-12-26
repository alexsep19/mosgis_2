package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface LicenseLocal extends CRUDBackend {
    
    JsonObject getVocs      ();
    JsonObject select       (JsonObject p);
    JsonObject getDocuments (String id, JsonObject p);
    JsonObject getHouses    (String id, JsonObject p);
    
}
