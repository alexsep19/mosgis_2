package ru.eludia.products.mosgis.jms.xl.base;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;

public abstract class XLMDB extends UUIDMDB<InXlFile> {

    @Override
    protected Class getTableClass () {
        return InXlFile.class;
    }
    
    protected final XSSFWorkbook readWorkbook (DB db, UUID uuid) throws SQLException {
        
        XSSFWorkbook [] x = new XSSFWorkbook [] {null};
        
        db.forFirst (db.getModel ().get (InXlFile.class, uuid, "body"), (rs) -> {
            
            try {
                x [0] = new XSSFWorkbook (rs.getBlob (1).getBinaryStream ());
            }
            catch (IOException ex) {
                throw new IllegalStateException (ex);
            }

        });
        
        return x [0];
        
    }    

    protected void completeOK (DB db, UUID uuid, XSSFWorkbook wb) throws SQLException {
        setStatus (db, uuid, VocFileStatus.i.PROCESSED_OK);
        storeResult (db, uuid, wb);
    }

    protected void completeFail (DB db, UUID uuid, XSSFWorkbook wb) throws SQLException {        
        setStatus (db, uuid, VocFileStatus.i.PROCESSED_FAILED);        
        storeResult (db, uuid, wb);        
    }

    private void storeResult (DB db, UUID uuid, XSSFWorkbook wb) throws SQLException {
        
        final Connection cn = db.getConnection ();
        
        cn.setAutoCommit (false);
        
        try (PreparedStatement st = cn.prepareStatement ("SELECT errr FROM in_xl_files WHERE uuid = ? FOR UPDATE")) {

            st.setString (1, uuid.toString ().replace ("-", "").toUpperCase ());

            try (ResultSet rs = st.executeQuery ()) {

                if (rs.next ()) {

                    Blob blob = rs.getBlob (1);

                    try (OutputStream os = blob.setBinaryStream (0L)) {
                        wb.write (os);
                    }
                    catch (IOException ex) {
                        logger.log (Level.SEVERE, "Cannot store errors", ex);
                    }

                }

            }
            
            cn.commit ();
            cn.setAutoCommit (true);
            
        }
        
    }
    
    @Override
    public final void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws Exception {
        
        if (!isProcessing (r)) return;        

        XSSFWorkbook wb = null;
        
        try {
            wb = readWorkbook (db, uuid);
        }
        catch (Exception ex) {            
            logger.log (Level.SEVERE, "Cannot parse XL", ex);            
            setStatus (db, uuid, VocFileStatus.i.PROCESSED_FAILED);
        }

        try {
            handleWorkbook (wb, uuid, db);
        }
        catch (Exception ex) {            
            logger.log (Level.SEVERE, "Cannot parse XL", ex);            
            setStatus (db, uuid, VocFileStatus.i.PROCESSED_FAILED);
        }

    }

    public final void setStatus (DB db, UUID uuid, VocFileStatus.i status) throws SQLException {
        
        db.update (InXlFile.class, DB.HASH (
            EnTable.c.UUID, uuid,
            InXlFile.c.ID_STATUS, status.getId ()
        ));
        
    }

    protected final void handleWorkbook (XSSFWorkbook wb, UUID uuid, DB db) throws SQLException {
        
        try {
            processLines (wb, uuid, db);
            completeOK (db, uuid, wb);
        }
        catch (XLException e) {
            completeFail (db, uuid, wb);
        }
        catch (Exception e) {
            logger.log (Level.SEVERE, "Cannot process XL", e);
            completeFail (db, uuid, wb);
        }
        
    }
    
    protected abstract void processLines (XSSFWorkbook wb, UUID uuid, DB db) throws Exception;

    protected final boolean isProcessing (Map<String, Object> r) {
        
        try {
            VocFileStatus.i status = VocFileStatus.i.forId (r.get (InXlFile.c.ID_STATUS.lc ()));
            if (status != VocFileStatus.i.PROCESSING) {
                logger.warning ("Wrong status: " + status.name () + ", bailing out");
                return false;
            }
        }
        catch (Exception e) {
            logger.log (Level.SEVERE, "Cannot get status", e);
            return false;
        }
        
        return true;
        
    }

}
