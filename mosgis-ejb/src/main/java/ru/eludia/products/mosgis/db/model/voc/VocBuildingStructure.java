package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocBuildingStructure extends Table {
    
    public VocBuildingStructure () {
                
        super ("vc_fias_strstat", "Типы сооружений в ФИАС");
        
        pk    ("strstatid",    Type.INTEGER,                                      "Код в ФИАС");        
        
        col   ("name",         Type.STRING,                                       "Наименование");
        col   ("shortname",    Type.STRING, 10, null,                             "Сокр.");
        
    }

}