package ru.eludia.products.mosgis.jms.xl.base;

import java.io.IOException;
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

    protected void completeOK (DB db, UUID parent) throws SQLException {
        setStatus (db, parent, VocFileStatus.i.PROCESSED_OK);
    }

    protected void completeFail (DB db, UUID parent) throws SQLException {
        setStatus (db, parent, VocFileStatus.i.PROCESSED_FAILED);
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

    protected final void setStatus (DB db, UUID uuid, VocFileStatus.i status) throws SQLException {
        
        db.update (InXlFile.class, DB.HASH (
            EnTable.c.UUID, uuid,
            InXlFile.c.ID_STATUS, status.getId ()
        ));
        
    }

    protected final void handleWorkbook (XSSFWorkbook wb, UUID uuid, DB db) throws SQLException {
        
        try {
            processLines (wb, uuid, db);
            completeOK (db, uuid);
        }
        catch (XLException e) {
            completeFail (db, uuid);
        }
        catch (Exception e) {
            logger.log (Level.SEVERE, "Cannot process XL", e);
            completeFail (db, uuid);
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
