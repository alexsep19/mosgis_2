package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.gis.HouseGis;
import ru.eludia.products.mosgis.db.model.gis.PremiseGis;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;

public abstract class Passport extends Table {
    
    protected Logger logger = java.util.logging.Logger.getLogger (this.getClass ().getName ());

    public static final Class [] classes = new Class [] {HouseGis.class, PremiseGis.class, House.class, ResidentialPremise.class, NonResidentialPremise.class, Block.class, LivingRoom.class};
    
    List<MultipleRefTable> refTables = new ArrayList ();

    public final List<MultipleRefTable> getRefTables () {
        return refTables;
    }

    public Passport (String name, String remark) {
        super (name, remark);
    }
    
    public abstract void addNsiFields (DB db) throws SQLException;

    public final void update (DB db) throws SQLException {
        
        logger.info ("Adding NSI fields...");
        
        addNsiFields (db);
        
        logger.info ("Updating DB schema: " + getColumns ().values ());

        db.updateSchema (this);
        
        logger.info ("Updating m2m tables: " + getRefTables ());

        for (MultipleRefTable refTable: getRefTables ()) db.updateSchema (refTable);

    }

}