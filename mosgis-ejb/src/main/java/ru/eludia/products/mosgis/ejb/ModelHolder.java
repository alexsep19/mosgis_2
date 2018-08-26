package ru.eludia.products.mosgis.ejb;

import java.sql.SQLException;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;
import ru.eludia.products.mosgis.db.model.MosGisModel;

@Startup
@Singleton
public class ModelHolder {
    
    @Resource (name = "jdbc/mosgis")
    private DataSource ds;

    private static MosGisModel model;

    public static final MosGisModel getModel () {
        return model;
    }
    
    @PostConstruct
    void init () {
        
        try {            
            model = new MosGisModel (ds);
            model.update ();
        }
        catch (SQLException ex) {
            throw new IllegalStateException (ex);
        }

    }
    
}