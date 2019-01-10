package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface VocOrganizationProposalsLocal extends CRUDBackend {
    JsonObject select       (JsonObject p);
    JsonObject getItem      (String id);
    JsonObject getVocs      ();
    JsonObject doCreate     (JsonObject p, User user);
    JsonObject doDelete     (String id, User user);
    JsonObject doApprove    (String id, User user);
}