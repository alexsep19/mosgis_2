package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface OutSoapExportNsiItemLocal {
    
    JsonObject getStats  (JsonObject p);
    JsonObject getErrors (String dt, JsonObject p);
    JsonObject getRq     (String id);
    JsonObject getRp     (String id);

}