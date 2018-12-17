package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;
import ru.eludia.products.mosgis.rest.User;

@Local
public interface LicenseLocal extends CRUDBackend {
    
    JsonObject getVocs ();
    JsonObject doReload    (String id, User user);
    JsonObject doRefresh   (String id, User user);
    
}
