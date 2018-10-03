package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface MgmtContractLocal extends CRUDBackend {
    
    JsonObject getVocs ();
    JsonObject doApprove   (String id, User user);
    JsonObject doAlter     (String id, User user);
    JsonObject doPromote   (String id);
    JsonObject doRefresh   (String id, User user);
    JsonObject doTerminate (String id, JsonObject p, User user);
    JsonObject doAnnul     (String id, JsonObject p, User user);
    JsonObject doRollover  (String id, JsonObject p, User user);

}