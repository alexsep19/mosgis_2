package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocOksm extends Table {

    public VocOksm () {

        super ("vc_oksm", "Общероссийский классификатор стран мира");

        pk    ("code",           Type.STRING,           "Код");
        col   ("shortname",      Type.STRING,           "Полное наименование страны");
        col   ("fullname",       Type.STRING,           "Краткое наименование");
        col   ("alfa3",          Type.STRING,           "Код альфа-3");
        col   ("alfa2",          Type.STRING,           "Код альфа-2");
    }

}