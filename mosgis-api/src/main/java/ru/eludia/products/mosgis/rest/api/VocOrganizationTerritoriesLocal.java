package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface VocOrganizationTerritoriesLocal {

    public JsonObject select(JsonObject p);
    public JsonObject getItem(String id);

}
