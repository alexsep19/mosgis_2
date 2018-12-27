package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.api.base.PassportBackend;

@Local
public interface ResidentialPremisesLocal extends PassportBackend {
    
    public JsonObject doRestore (String id);
    
}