package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.incoming.InFias;

public class VocStreet extends Table {
    
    public VocStreet () {
        
        super ("vc_streets", "Улицы (площади и прочие адресные объекты ФИАС 7-го уровня)");
        
        pk    ("aoguid",       Type.UUID,                                         "Код ФИАС");        
        col   ("formalname",   Type.STRING,                                       "Наименование (Якиманка)");
        col   ("formalname_uc",Type.STRING,  new Virt ("UPPER(\"FORMALNAME\")"),  "НАИМЕНОВАНИЕ (ЯКИМАНКА)");
        col   ("shortname",    Type.STRING,                                       "Сокращение (ул., пер. и т. п.)");
        col   ("livestatus",   Type.BOOLEAN, Bool.TRUE,                           "1 для актуальных записей, 0 для удалённых");

        fk    ("uuid_in_fias", InFias.class,                                      "Последний пакет импорта");
        
        key   ("f_uc", "formalname_uc", "livestatus");

    }

}