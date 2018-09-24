package ru.eludia.products.mosgis.db.model;

import java.sql.SQLException;
import javax.sql.DataSource;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.Passport;

public final class MosGisModel extends ru.eludia.base.Model {

    public Table getLogTable (Table t) {
        return get (t.getName () + "__log");
    }
    
    public MosGisModel (DataSource ds) throws SQLException {
        
        super (ds
            , "ru.eludia.products.mosgis.db.model.gis"
            , "ru.eludia.products.mosgis.db.model.tables"
            , "ru.eludia.products.mosgis.db.model.incoming.soap"
            , "ru.eludia.products.mosgis.db.model.incoming"
/*                
            , "ru.eludia.products.mosgis.db.model.incoming.json"
            , "ru.eludia.products.mosgis.db.model.incoming.json.statusViews"
            , "ru.eludia.products.mosgis.db.model.incoming.rec"
            , "ru.eludia.products.mosgis.db.model.incoming.rec.statusViews"
*/
            , "ru.eludia.products.mosgis.db.model.src"
            , "ru.eludia.products.mosgis.db.model.voc"
        );
        
        try (DB db = getDb ()) {
            for (Class c: Passport.classes) ((Passport) get (c)).addNsiFields (db);
            db.adjustModel ();
        }

    }

}