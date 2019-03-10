package ru.eludia.products.mosgis.ws.rest.impl.filestore;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.UploadedFile;
import ru.eludia.products.mosgis.filestore.FileStoreLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class FileStoreImpl implements FileStoreLocal {

    @Override
    public UUID store (String name, InputStream in) {
        
        final MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {            

            db.begin ();
            
            UUID uuid = (UUID) db.insertId (UploadedFile.class, DB.HASH (
                UploadedFile.c.LABEL, name
            ));
            
            try (PreparedStatement st = db.getConnection ().prepareStatement ("SELECT body FROM " + m.getName (UploadedFile.class) + " WHERE uuid = ? FOR UPDATE")) {

                st.setString (1, uuid.toString ().replace ("-", "").toUpperCase ());

                try (ResultSet rs = st.executeQuery ()) {

                    if (rs.next ()) {

                        Blob blob = rs.getBlob (1);
                        byte [] buffer = new byte [8 * 1024];
                        int len;

                        try (OutputStream out = blob.setBinaryStream (1)) {
                            while ((len = in.read (buffer)) > 0) out.write (buffer, 0, len);                        
                        }

                    }

                }

            }                    
                        
            db.commit ();
        
            return uuid;

        }
        catch (Exception ex) {
            throw new IllegalStateException (ex);
        }        
              
    }
    
}