package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface AgreementPaymentLocal extends CRUDBackend {

    JsonObject doApprove   (String id, User user);
    JsonObject doAlter     (String id, JsonObject p, User user);
    JsonObject doAnnul     (String id, JsonObject p, User user);
        
}