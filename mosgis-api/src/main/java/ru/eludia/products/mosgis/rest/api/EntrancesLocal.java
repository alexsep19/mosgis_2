package ru.eludia.products.mosgis.rest.api;

import java.util.List;
import javax.ejb.Local;
import javax.json.JsonObject;
import javax.json.JsonString;
import ru.eludia.products.mosgis.rest.api.base.PassportBackend;

@Local
public interface EntrancesLocal  extends PassportBackend {
    
    public JsonObject doRestore (String id, JsonObject p);
    
    public boolean checkCreate (String house_uuid, List<JsonString> nos);
    public boolean checkRestore (String id);
    
}