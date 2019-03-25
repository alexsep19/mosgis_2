package ru.eludia.products.mosgis.db.model.voc.nsi;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;

public class Nsi50 extends View {

    public static final String TABLE_NAME = "vw_nsi_50";

    public enum c implements ColEnum {

        ID     (Type.STRING, 20, null, "Код"),
        LABEL  (Type.STRING,     null, "Наименование"),
        OKEI   (VocOkei.class,         "Единицы измерения (ОКЕИ)"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public Nsi50 () {
        super  (TABLE_NAME, "Коды субъектов Российской Федерации (регионов)");
        cols   (c.class);
        pk     (c.ID);
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + "code" + " " + c.ID
            + ", " + VocNsi50.c.F_C5BA794FD6 + " " + c.LABEL
            + ", " + VocNsi50.c.F_C6E5A29665 + " " + c.OKEI
            + " FROM "
            + "  vc_nsi_50 "
            + " WHERE"
            + "  isactual=1"
        ;

    }

}