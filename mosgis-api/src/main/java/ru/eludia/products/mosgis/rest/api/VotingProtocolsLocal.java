package ru.eludia.products.mosgis.rest.api;

import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

public interface VotingProtocolsLocal extends CRUDBackend {
    
    public JsonObject getVocs ();

    public JsonObject doApprove (String id, User user);
    public JsonObject doAlter (String id, JsonObject p, User user);
    
}
