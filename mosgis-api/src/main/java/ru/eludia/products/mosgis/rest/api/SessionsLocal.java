package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface SessionsLocal {

    JsonObject create (String login, String password);

}