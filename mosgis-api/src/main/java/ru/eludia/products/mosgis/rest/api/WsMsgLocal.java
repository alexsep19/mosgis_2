package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface WsMsgLocal {

    JsonObject select(JsonObject p);

    JsonObject getVocs();

    JsonObject getRq(String id);

    JsonObject getRp(String id);

}
