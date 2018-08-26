package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.incoming.InFias;

public class VocBuilding extends Table {
    
    public VocBuilding () {
        
        super ("vc_buildings", "Здания, сооружения");
        
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
        
        col   ("house_label", Type.STRING,  new Virt ("DECODE(HOUSENUM,NULL,NULL,'д. '||HOUSENUM)"),  "д. ...");
        col   ("build_label", Type.STRING,  new Virt ("DECODE(BUILDNUM,NULL,NULL,'корп. '||BUILDNUM)"),  "корп. ...");

    }

}