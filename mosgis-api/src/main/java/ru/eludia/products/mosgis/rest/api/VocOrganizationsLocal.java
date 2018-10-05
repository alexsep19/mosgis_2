package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;

@Local
public interface VocOrganizationsLocal {
    
    JsonObject getVocs ();
    JsonObject select (JsonObject p);
    JsonObject getItem (String id);
    JsonObject doImport (JsonObject p);
    JsonObject getMgmtNsi58 (String id);
    JsonObject doRefresh   (String id, User user);

}