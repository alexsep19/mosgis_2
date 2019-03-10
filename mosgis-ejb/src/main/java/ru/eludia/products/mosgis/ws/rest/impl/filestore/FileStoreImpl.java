package ru.eludia.products.mosgis.ws.rest.impl.filestore;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.UploadedFile;
import ru.eludia.products.mosgis.db.model.tables.Sender;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.filestore.FileStoreLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class FileStoreImpl implements FileStoreLocal {

    private static final Logger logger = Logger.getLogger (FileStoreImpl.class.getName ());

    @Override
    public UUID store (UUID senderUuid, String name, InputStream in) {
        
        final MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {            

            db.begin ();
            
            UUID uuid = (UUID) db.insertId (UploadedFile.class, DB.HASH (
                UploadedFile.c.LABEL,       name,
                UploadedFile.c.UUID_SENDER, senderUuid
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

    @Override
    public UUID getSenderUuid (String login, String password) {
        
        final MosGisModel m = ModelHolder.getModel();

        try (DB db = m.getDb ()) {
            
            Map<String, Object> r = db.getMap (m
                .select (Sender.class, "*")
                .where  (Sender.c.LOGIN.lc(), login)
                .and    (EnTable.c.IS_DELETED, 0)
            );
            
            if (r == null) return null;
            
            final UUID salt = (UUID) r.get (Sender.c.SALT.lc ());
            
            final String asIs = VocUser.encrypt (salt, password);

            final String toBe = DB.to.String (r.get (Sender.c.SHA1.lc ()));

            if (DB.eq (asIs, toBe)) {
                return (UUID) r.get (EnTable.c.UUID.lc ());
            }
            else {
                logger.warning ("Wrong BASIC password supplied for " + login);
                return null;
            }
            
        } catch (SQLException ex) {
            throw new IllegalStateException (ex);
        }


    }
    
}