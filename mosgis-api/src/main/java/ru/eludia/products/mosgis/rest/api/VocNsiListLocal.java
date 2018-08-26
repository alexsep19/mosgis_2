package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface VocNsiListLocal {
    
    JsonObject select   (JsonObject p);
    JsonObject getItem  (String id);
    JsonObject getLines (String id, JsonObject p);
    JsonObject getVocs  (JsonObject p);
    JsonObject doImport (String id);

}