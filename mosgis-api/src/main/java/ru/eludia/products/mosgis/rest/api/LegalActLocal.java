package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;
import ru.eludia.products.mosgis.rest.api.base.FileBackend;

@Local
public interface LegalActLocal extends CRUDBackend, FileBackend {
    JsonObject getVocs ();
    JsonObject doEdit (String id, JsonObject p, User user);
    JsonObject doApprove   (String id, User user);
    JsonObject doAlter     (String id, User user);
    JsonObject doAnnul     (String id, JsonObject p, User user);
}