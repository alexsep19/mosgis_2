package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface MeteringDeviceLocal extends CRUDBackend {    
        
    JsonObject getVocs ();
    JsonObject doSetAccounts (String id, JsonObject p, User user);
    JsonObject doUnsetAccounts (String id, JsonObject p, User user);
    JsonObject doSetMeters   (String id, JsonObject p, User user);
    JsonObject doUnsetMeters (String id, JsonObject p, User user);
   
//    JsonObject doApprove   (String id, User user);
//    JsonObject doAlter     (String id, User user);

        
}