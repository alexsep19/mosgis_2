package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface SenderLocal extends CRUDBackend {
    
    JsonObject doSetPassword (String id, String password, User user);
    JsonObject doLock        (String id, String password, User user);
    JsonObject doUnlock      (String id, String password, User user);
        
}