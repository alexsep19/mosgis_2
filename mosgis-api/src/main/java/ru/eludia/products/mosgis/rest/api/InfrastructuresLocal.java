package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface InfrastructuresLocal extends CRUDBackend {
    
    JsonObject doAlter     (String id, User user);
    JsonObject doApprove   (String id, User user);
    JsonObject getVocs ();

}