package ru.eludia.products.mosgis.db.model;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Passport;

public final class MosGisModel extends ru.eludia.base.Model {

    private static Logger logger = Logger.getLogger (MosGisModel.class.getName ());
    
    private static final String REGISTRYNUMBER = "registrynumber";
    private static final String VOCNSI58 = "vc_nsi_58";
    
    public Table getLogTable (Table t) {
        return get (t.getName () + "__log");
    }
    
    public MosGisModel (DataSource ds) throws SQLException {
        
        super (ds
            , "ru.eludia.products.mosgis.db.model.gis"
            , "ru.eludia.products.mosgis.db.model.tables"
            , "ru.eludia.products.mosgis.db.model.incoming.soap"
            , "ru.eludia.products.mosgis.db.model.incoming"
            , "ru.eludia.products.mosgis.db.model.incoming.nsi"
/*                
            , "ru.eludia.products.mosgis.db.model.incoming.json"
            , "ru.eludia.products.mosgis.db.model.incoming.json.statusViews"
            , "ru.eludia.products.mosgis.db.model.incoming.rec"
            , "ru.eludia.products.mosgis.db.model.incoming.rec.statusViews"
*/
            , "ru.eludia.products.mosgis.db.model.src"
            , "ru.eludia.products.mosgis.db.model.voc"
        );
        
        logger.log (Level.INFO, "PACKAGES LOADED");
        
        try (DB db = getDb ()) {
            
            try {
                
                db.forEach ((
                        
                    select (VocNsiList.class, REGISTRYNUMBER))
                        .and ("cols IS NOT NULL")
                        .orderBy (REGISTRYNUMBER)

                , rs -> {
                    
                    final int n = rs.getInt (REGISTRYNUMBER);
                    
                    if (tables.containsKey (NsiTable.getName (n))) return;                    

                    try {
                        add (new NsiTable (db, n));
                    }
                    catch (Exception ex) {
                        logger.log (Level.WARNING, "Exception occured while adding NSI table " + n, ex);
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

            for (Class c: Passport.classes) ((Passport) get (c)).addNsiFields (db);
            db.adjustModel ();        
        }
    }
}