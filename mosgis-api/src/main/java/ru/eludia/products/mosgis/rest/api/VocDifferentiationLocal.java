package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;

@Local
public interface VocDifferentiationLocal {
    
    JsonObject select   (JsonObject p);
    JsonObject doImport (User user);
    JsonObject getLog   ();

}