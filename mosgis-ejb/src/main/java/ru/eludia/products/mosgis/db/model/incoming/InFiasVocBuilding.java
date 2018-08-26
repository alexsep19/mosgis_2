package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.products.mosgis.db.model.voc.*;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.def.Bool;

public class InFiasVocBuilding extends Table {
    
    public InFiasVocBuilding () {
        
        super ("in_fias_vc_buildings", "Здания, сооружения");
        
        pk    ("houseguid",    Type.UUID,                                         "Код здания в ФИАС");        
        
        col   ("housenum",     Type.STRING, 20, null,                             "Номер дома");
        col   ("buildnum",     Type.STRING, 10, null,                             "Номер корпуса");
        col   ("strucnum",     Type.STRING, 10, null,                             "Номер строения");
        fk    ("strstatus",    VocBuildingStructure.class,                        "Признак строения");

        fk    ("eststatus",    VocBuildingEstate.class,                           "Признак владения");

        col   ("postalcode",   Type.INTEGER, 6, null,                             "Почтовый индекс");
        col   ("okato",        Type.INTEGER, 11, null,                            "ОКАТО");
        col   ("oktmo",        Type.INTEGER, 11, null,                            "ОКТМО");
        col   ("cadnum",       Type.STRING, 100, null,                            "Кадастровый номер");
        
        col   ("livestatus",   Type.BOOLEAN, Bool.TRUE,                           "1 для актуальных записей, 0 для удалённых");

        fk    ("aoguid",       VocStreet.class,                                   "Улица (площадь и т. п.)");
        fk    ("uuid_in_fias", InFias.class,                                      "Последний пакет импорта");
        
    }

}