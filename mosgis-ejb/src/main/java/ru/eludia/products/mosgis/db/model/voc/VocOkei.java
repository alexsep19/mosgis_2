package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocOkei extends Table {

    public VocOkei () {

        super ("vc_okei", "Общероссийский классификатор единиц измерения");

        pk    ("code",           Type.STRING,           "Код");        
        col   ("name",           Type.STRING,           "Наименование единицы измерения");
        col   ("national",       Type.STRING,           "Условное обозначение национальное");

    }

}