package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocBuildingEstate extends Table {
    
    public VocBuildingEstate () {
                
        super ("vc_fias_eststat", "Типы владений в ФИАС");
        
        pk    ("eststatid",    Type.INTEGER,                                      "Код в ФИАС");        
        
        col   ("name",         Type.STRING,                                       "Наименование");
        
    }

}