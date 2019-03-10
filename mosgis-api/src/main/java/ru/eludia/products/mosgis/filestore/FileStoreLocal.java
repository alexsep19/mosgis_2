package ru.eludia.products.mosgis.filestore;

import java.io.InputStream;
import java.util.UUID;
import javax.ejb.Local;

@Local
public interface FileStoreLocal {
    
    UUID store (String name, InputStream is);
    
}