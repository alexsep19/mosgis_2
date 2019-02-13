package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface MeteringDeviceLocal extends CRUDBackend {    
        
    JsonObject getVocs ();
   
//    JsonObject doApprove   (String id, User user);
//    JsonObject doAlter     (String id, User user);
        
}