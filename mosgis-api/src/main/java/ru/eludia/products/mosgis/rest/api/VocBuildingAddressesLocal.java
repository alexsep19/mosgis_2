package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface VocBuildingAddressesLocal {
    
    JsonObject select (JsonObject p);

}