package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface VcRd1Local {
    
    JsonObject select (JsonObject p);
    JsonObject getVocs ();

}