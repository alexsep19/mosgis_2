package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface CitizenCompensationCategoryLocal extends CRUDBackend {
    
    public JsonObject getVocs ();
    public JsonObject getCalculation (String id);
    public JsonObject getLegalActs (String id);
    public JsonObject doImport(JsonObject p, User user);
    public JsonObject getLog();
}