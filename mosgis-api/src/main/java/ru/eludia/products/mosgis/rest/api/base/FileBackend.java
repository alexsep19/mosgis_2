package ru.eludia.products.mosgis.rest.api.base;

import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.WebApplicationException;

public interface FileBackend {
    
    void download (String id, OutputStream output) throws IOException, WebApplicationException;

}