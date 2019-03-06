package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;

@Local
public interface VocBicLocal {
    
    JsonObject select   (JsonObject p);
//    JsonObject getItem  (String id);
//    JsonObject getLines (String id, JsonObject p);
    JsonObject getVocs  (JsonObject p);
    JsonObject doImport (User user);
    JsonObject getLog   ();

}