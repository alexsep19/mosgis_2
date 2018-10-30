package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface HousesLocal {
    
    JsonObject select (JsonObject p);
    JsonObject getItem (String id);
    JsonObject doUpdate (String id, JsonObject p);
    JsonObject doPatch  (String id, JsonObject p);
    JsonObject getVocPassportFields (String id, Integer[] ids);
    JsonObject doSetMultiple (String id, JsonObject p);
    JsonObject doCreate (JsonObject p);

}