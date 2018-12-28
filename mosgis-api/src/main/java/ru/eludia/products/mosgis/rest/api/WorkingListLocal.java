package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface WorkingListLocal extends CRUDBackend {
    
    JsonObject doAddItems (String id, JsonObject p, User user);
    JsonObject doApprove   (String id, User user);
    JsonObject doCancel    (String id, User user);
    JsonObject doAlter     (String id, JsonObject p, User user);
//    JsonObject doAnnul     (String id, JsonObject p, User user);
        
}