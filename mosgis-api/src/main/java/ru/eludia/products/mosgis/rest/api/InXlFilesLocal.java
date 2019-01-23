package ru.eludia.products.mosgis.rest.api;

import java.io.IOException;
import java.io.OutputStream;
import javax.ejb.Local;
import javax.json.JsonObject;
import javax.ws.rs.WebApplicationException;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;
import ru.eludia.products.mosgis.rest.api.base.FileBackend;

@Local
public interface InXlFilesLocal extends CRUDBackend, FileBackend {
    
    JsonObject getVocs ();
    void download_errors (String id, OutputStream output) throws IOException, WebApplicationException;

}