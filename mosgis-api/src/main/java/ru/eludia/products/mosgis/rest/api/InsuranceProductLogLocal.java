package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.FileBackend;

@Local
public interface InsuranceProductLogLocal extends FileBackend {
    
    JsonObject select  (JsonObject p, User user);
    JsonObject getItem (String id);

}