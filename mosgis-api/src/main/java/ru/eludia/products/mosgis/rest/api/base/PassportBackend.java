package ru.eludia.products.mosgis.rest.api.base;

import javax.json.JsonObject;

public interface PassportBackend extends CRUDBackend {
    
    JsonObject getVocPassportFields (String id, Integer[] ids);
    JsonObject doSetMultiple (String id, JsonObject p);

}