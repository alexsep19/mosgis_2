package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;

@Local
public interface OverhaulShortProgramDocumentsLocal extends CRUDBackend {
    
    JsonObject getVocs ();
    
}
