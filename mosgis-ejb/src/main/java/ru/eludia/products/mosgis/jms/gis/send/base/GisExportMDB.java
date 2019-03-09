package ru.eludia.products.mosgis.jms.gis.send.base;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.jms.Queue;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;

public abstract class GisExportMDB <LT extends Table> extends UUIDMDB<LT> {
    
    @EJB
    protected UUIDPublisher uuidPublisher;

    protected abstract Queue getFilesQueue ();
    protected abstract Table getFileLogTable ();
    protected abstract Col   getStatusCol ();
    
    private String getStatusColName () {
        return getStatusCol ().getName ();
    }
    
    protected final Table getEnTable () {
        final String name = getTable ().getName ();
        return ModelHolder.getModel ().get (name.substring (0, name.length () - "__log".length ()));
    }    
    
    protected void store (DB db, AckRequest.Ack ack, Map<String, Object> r, VocGisStatus.i nextStatus) throws SQLException {
        
        db.begin ();
        
            OutSoap.registerAck (db, ack);

            db.update (getTable (), DB.HASH (
                "uuid",          r.get ("uuid"),
                "uuid_out_soap", r.get ("uuid"),
                "uuid_message",  ack.getMessageGUID ()
            ));

            db.update (getEnTable (), DB.HASH (
                "uuid",          r.get ("uuid_object"),
                "uuid_out_soap", r.get ("uuid"),
                getStatusColName (), nextStatus.getId ()
            ));
        
        db.commit ();
        
    }
    
    protected void fail (DB db, ru.gosuslugi.dom.schema.integration.base.Fault faultInfo, Map<String, Object> r, VocGisStatus.i failStatus) throws SQLException {

        Object uuid = r.get ("uuid");

        db.begin ();
        
            OutSoap.registerFault (db, uuid, faultInfo);

            db.update (getTable (), DB.HASH (
                "uuid",          uuid,
                "uuid_out_soap", uuid
            ));

            db.update (getEnTable (), DB.HASH (
                "uuid",              r.get ("uuid_object"),
                "uuid_out_soap",     uuid,
                getStatusColName (), failStatus.getId ()
            ));
        
        db.commit ();
    }
    
    protected void fail (DB db, String action, VocGisStatus.i status, Exception ex, Map<String, Object> r) throws SQLException {
        
        Object uuid = r.get ("uuid");
        
        db.begin ();
        
            OutSoap.registerException (db, uuid, getClass ().getName (), action, ex);
        
            db.update (getTable (), DB.HASH (
                "uuid",          uuid,
                "uuid_out_soap", uuid
            ));
        
            db.update (getEnTable (), DB.HASH (
                "uuid",              r.get ("uuid_object"),
                "uuid_out_soap",     uuid,
                getStatusColName (), status.getId ()
            ));
        
        db.commit ();
        
    }
    
    protected boolean startUpload (DB db, Map<String, Object> f) throws SQLException {
        
        logger.info ("Sending " + f.get ("label"));
        
        db.update (getFileLogTable (), DB.HASH (
            "uuid",             f.get ("id_log"),
            "ts_start_sending", NOW
        ));
        
        uuidPublisher.publish (getFilesQueue (), (UUID) f.get ("uuid"));
        
        return true;
        
    }
    
    protected boolean isWaitingForFile (Map<String, Object> f, DB db) throws Exception {

        final Object err = f.get ("log.err_text"); 
        if (DB.ok (err)) throw new Exception (err.toString ());
        
        if (DB.ok (f.get ("attachmentguid"))) return false;

        if (!DB.ok (f.get ("log.ts_start_sending"))) return startUpload (db, f);
        
        logger.info ("Waiting for " + f.get ("label") + " to be upoaded...");        
        return true;
        
    }    
    
    protected boolean isWaiting (final List<Map<String, Object>> files, DB db, VocGisStatus.i status, Map<String, Object> r) throws SQLException {

        boolean isWaitingForSomeFile = false;

        try {
            for (Map<String, Object> file: files) isWaitingForSomeFile = isWaitingForSomeFile || isWaitingForFile (file, db);
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot send file(s)", ex);
            fail (db, "send_file", status, ex, r);
            return true;
        }
        
        if (isWaitingForSomeFile) uuidPublisher.publish (ownDestination, (UUID) r.get ("uuid"));
        
        return isWaitingForSomeFile;
        
    }
    
}