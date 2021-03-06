package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface VocOrganizationsLocal extends CRUDBackend {
    
    JsonObject getVocs      ();
    JsonObject select       (JsonObject p, User user);
    JsonObject list         (JsonObject p);
    JsonObject doImport     (JsonObject p, User user);
    JsonObject getMgmtNsi58 (String id);
    JsonObject getHours   (String id);
    JsonObject doRefresh    (String id, User user);
    JsonObject doPatch      (String id, JsonObject p, User user);
    JsonObject doPatchHours (String id, JsonObject p);
    JsonObject doImportMgmtContracts (String id, User user);
    JsonObject doImportSrContracts (String id, User user);
    JsonObject doImportAddServices   (String id, User user);
    JsonObject doImportCharters      (String id, User user);
    JsonObject doImportAccounts      (String id, User user);

}