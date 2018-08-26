package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface VocRdVocLocal {
    
    JsonObject getItem  (String id);
    JsonObject getLines (String id, JsonObject p);
    JsonObject doImport (String id);
    JsonObject doSetNsi (String id, JsonObject p);

}