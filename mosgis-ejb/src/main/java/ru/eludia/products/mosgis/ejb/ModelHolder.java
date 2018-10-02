package ru.eludia.products.mosgis.ejb;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
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
    
    private static Logger logger = Logger.getLogger (ModelHolder.class.getName ());
    
    private static AtomicReference<MosGisModel> modelReference = new AtomicReference<MosGisModel>();

    public static final MosGisModel getModel () {
        return modelReference.get();
    }
    
    @PermitAll
    public void updateModel () {
        
        logger.log (Level.INFO, "MODEL UPDATE STARTED");
        
        MosGisModel newModel;
        
        try {
            newModel = new MosGisModel (ds);
            newModel.update ();
            modelReference.getAndSet (newModel);
        }
        catch (SQLException ex) {
            throw new IllegalStateException (ex);
        }
    }
    
    @PostConstruct
    void init () {
        
        MosGisModel model;
        
        try {            
            model = new MosGisModel (ds);
            model.update ();
            modelReference.set (model);
        }
        catch (SQLException ex) {
            throw new IllegalStateException (ex);
        }

    }
    
}