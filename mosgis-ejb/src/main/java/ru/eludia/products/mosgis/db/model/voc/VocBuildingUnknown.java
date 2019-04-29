package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.tables.House;

public class VocBuildingUnknown extends View {
    
    public static final String TABLE_NAME = "vw_buildings_unknown";

    public VocBuildingUnknown () {
        super  (TABLE_NAME, "GUID ФИАС, не опрошенные в ГИС ЖКХ на предмет паспорта дома");
        cols   (c.class);
        pk     (c.FIASHOUSEGUID);
    }
    
    public enum c implements ColEnum {
        
	FIASHOUSEGUID (VocBuilding.class, "GUID ФИАС"),
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }        

    @Override
    public String getSQL () {
        return 
            "SELECT houseguid fiashouseguid FROM " + VocBuilding.TABLE_NAME
                + " MINUS " +
            "SELECT fiashouseguid FROM " + House.TABLE_NAME
                + " MINUS " +
            "SELECT uuid_object fiashouseguid FROM " + VocBuildingLog.TABLE_NAME
        ;
    }
    
}