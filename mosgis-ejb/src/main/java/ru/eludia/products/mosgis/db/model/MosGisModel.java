package ru.eludia.products.mosgis.db.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Passport;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.rest.User;

public final class MosGisModel extends ru.eludia.base.Model {

    private static Logger logger = Logger.getLogger (MosGisModel.class.getName ());
    
    private static final String REGISTRYNUMBER = "registrynumber";
    private static final String VOCNSI58 = "vc_nsi_58";
    private static final String NAME = "name";
    private static final String ISNESTED = "is_nested";
    private static final String COLS = "cols";
    
    public Table getLogTable (Table t) {
        return get (t.getName () + "__log");
    }
    
    public String createIdLog (DB db, Table table, User user, Object id, VocAction.i action) throws SQLException {

        Table logTable = getLogTable (table);

        if (logTable == null) return null;
        
        String uuidUser = null;        
        if (user != null && !"-1".equals (user.getId ())) uuidUser = user.getId ();
        
        String idLog = db.insertId (logTable, HASH (
            "action", action,
            "uuid_object", id,
            "uuid_user", uuidUser
        )).toString ();
        
        final Map<String, Object> h = HASH (
            table.getPk ().get (0).getName (), id,
            "id_log",                          idLog
        );
        
        Col idStatusCol = table.getColumn ("id_status");
        
        if (idStatusCol != null && idStatusCol instanceof Ref) {
            Ref ref = (Ref) idStatusCol;
            if (VocAsyncEntityState.TABLE_NAME.equalsIgnoreCase (ref.getTargetTable ().getName ())) h.put ("id_status", VocAsyncEntityState.i.PENDING.getId ());
        }
        
        db.update (table, h);
                
        return idLog;
        
    }
    
    public String createIdLogWs (DB db, Class c, UUID uuidInSoap, Object id, VocAction.i action) throws SQLException {
        return createIdLogWs (db, get (c), uuidInSoap, id, action);
    }

    public String createIdLogWs (DB db, Table table, UUID uuidInSoap, Object id, VocAction.i action) throws SQLException {

        Table logTable = getLogTable (table);

        if (logTable == null) return null;
        
        String idLog = db.insertId (logTable, HASH (
            "action", action,
            "uuid_object", id,
            "uuid_in_soap", uuidInSoap
        )).toString ();
        
        db.update (table, HASH (
            table.getPk ().get (0).getName (), id,
            "id_status",                       VocAsyncEntityState.i.PENDING.getId (),
            "id_log",                          idLog
        ));
                
        return idLog;
        
    }
    
    
    public MosGisModel (DataSource ds) throws SQLException, IOException {
        
        super (ds
            , "ru.eludia.products.mosgis.db.model.gis"
            , "ru.eludia.products.mosgis.db.model.tables"
            , "ru.eludia.products.mosgis.db.model.incoming.soap"
            , "ru.eludia.products.mosgis.db.model.incoming"
            , "ru.eludia.products.mosgis.db.model.incoming.nsi"
            , "ru.eludia.products.mosgis.db.model.incoming.fias"
            , "ru.eludia.products.mosgis.db.model.incoming.xl"
            , "ru.eludia.products.mosgis.db.model.incoming.xl.lines"
/*                
            , "ru.eludia.products.mosgis.db.model.incoming.json"
            , "ru.eludia.products.mosgis.db.model.incoming.json.statusViews"
            , "ru.eludia.products.mosgis.db.model.incoming.rec"
            , "ru.eludia.products.mosgis.db.model.incoming.rec.statusViews"
*/
            , "ru.eludia.products.mosgis.db.model.src"
            , "ru.eludia.products.mosgis.db.model.voc"
            , "ru.eludia.products.mosgis.db.model.voc.nsi"
            , "ru.eludia.products.mosgis.db.model.ws"
        );
        
        logger.log (Level.INFO, "PACKAGES LOADED");
        
        try (DB db = getDb ()) {
            
            try {
                
                db.forEach ((
                        
                    select (VocNsiList.class, REGISTRYNUMBER, NAME, ISNESTED, COLS))
                        .and ("cols IS NOT NULL")
                        .orderBy (REGISTRYNUMBER)

                , rs -> {
                    
                    final String name = NsiTable.getName (rs.getInt (REGISTRYNUMBER));
                    
                    tables.remove (name);

                    try {
                        add (new NsiTable (db, rs));
                    }
                    catch (Exception ex) {
                        logger.log (Level.WARNING, "Exception occured while adding NSI table " + rs.getInt (REGISTRYNUMBER), ex);
                    }
                    
                });

            }
            catch (Exception ex) {
                logger.log (Level.WARNING, "Exception occured while adding NSI tables: " + ex.getLocalizedMessage ());
            }
            
            logger.log (Level.INFO, "NSI TABLES ADDED");

            try {
                if (!tables.containsKey(VOCNSI58)) {
                    super.addPackage("ru.eludia.products.mosgis.db.model.nsi.nsi58");
                }
            }
            catch (Exception ex)
            {
                logger.log (Level.WARNING, "Exception occured while cheching NSI 58: " + ex.getLocalizedMessage ());
            }

            logger.log (Level.INFO, "VOC NSI 58 CHECKED");
            
            try {
                for (Class c: Passport.classes) ((Passport) get (c)).addNsiFields (db);
            }
            catch (SQLException ex) {                
                if (ex.getErrorCode () != 942) throw ex;
                logger.log (Level.WARNING, "Problem with passport fields, OK for an empty database", ex);
            }
            
            db.adjustModel ();        
            
        }
        
    }
    
}