package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.ModelHolder;

public class VocOkei extends Table {

    public static final String TABLE_NAME = "vc_okei";

    public static final String CODES_ENERGY_WT = "243,245"; // Ватт-час, Киловатт-час

    public enum c implements ColEnum {

        CODE         (Type.STRING,  "Код"),
        NAME         (Type.STRING,  "Наименование единицы измерения"),
        NATIONAL     (Type.STRING,  "Условное обозначение национальное"),
        ;

        @Override public Col getCol() {return col;} private Col col; private c (Type type, Object... p) {col = new Col(this, type, p);}

    }

    public VocOkei () {

        super (TABLE_NAME, "Общероссийский классификатор единиц измерения");
        cols  (c.class);        
        pk    (c.CODE);        

    }

    public static Select getVocSelect () {

        return ModelHolder.getModel ().select (VocOkei.class
            , c.CODE.lc () + " AS id"
            , c.NATIONAL.lc () + " AS label"
        ).orderBy (c.NATIONAL);

    }

}