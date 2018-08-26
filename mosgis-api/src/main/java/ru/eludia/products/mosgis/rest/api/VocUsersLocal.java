package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface VocUsersLocal {
    
    JsonObject getVocs ();
    JsonObject select (JsonObject p);
    JsonObject getItem (String id);
    JsonObject doCreate (JsonObject p);
    JsonObject doUpdate (String id, JsonObject p);
    JsonObject doDelete (String id);
    JsonObject doSetPassword (String id, String password);

}