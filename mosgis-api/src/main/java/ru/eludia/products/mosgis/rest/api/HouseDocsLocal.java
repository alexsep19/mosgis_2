package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.api.base.FileBackend;

@Local
public interface HouseDocsLocal extends FileBackend {
    
    JsonObject select   (JsonObject p);
    JsonObject getItem  (String id);
    JsonObject doCreate (JsonObject p);
    JsonObject doUpdate (String id, JsonObject p);
    JsonObject doEdit   (String id, JsonObject p);
    JsonObject doDelete (String id);

}