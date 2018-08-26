package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface OpenDataLocal {
    
    JsonObject select (JsonObject p);
    JsonObject getLog (JsonObject p);

}