package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface CitizenCompensationCategoryLocal extends CRUDBackend {
    
    public JsonObject getVocs ();
    public JsonObject getCalculation (String id, User user);
//    public JsonObject doImport(User user);
}