package ru.eludia.products.mosgis.rest.api;

import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

public interface CheckPlansLocal extends CRUDBackend {
    
    public JsonObject getVocs ();
    
}