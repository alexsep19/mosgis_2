package ru.eludia.products.mosgis.ws.rest.impl.filestore;

import java.io.InputStream;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import ru.eludia.products.mosgis.filestore.FileStoreLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class FileStoreImpl implements FileStoreLocal {

    @Override
    public UUID store (String name, InputStream is) {
        return UUID.randomUUID ();
    }
    
}