package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;

public class AnyCitizenCompensation extends View {

    public static final String TABLE_NAME = "vw_cit_comps";

    public enum c implements ColEnum {
        ID                   (Type.UUID,     "id"),
        SNILS                (Type.NUMERIC, 11, null, "СНИЛС"),
        LABEL                (Type.STRING,      null, "ФИО"),
        LABEL_UC             (Type.STRING,      null, "ФИО (в верхнем регистре)"),
	ADDRESS              (Type.STRING,      null, "Адрес"),
	ADDRESS_UC           (Type.STRING,      null, "Адрес")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public AnyCitizenCompensation () {
        super  (TABLE_NAME, "Граждане, получающие компенсацию расходов");
        cols   (EnTable.c.class);
	cols   (CitizenCompensation.c.class);
        cols   (c.class);
        pk     (c.ID);
    }

    @Override
    public final String getSQL () {

        StringBuilder sb = new StringBuilder ("SELECT ");

        for (EnTable.c i: EnTable.c.values ()) {
            sb.append ("o.");
            sb.append (i.lc ());
            sb.append (',');
        }

        for (CitizenCompensation.c i: CitizenCompensation.c.values ()) {
            sb.append ("o.");
            sb.append (i.lc ());
            sb.append (',');
        }

        return sb.toString ()
            + " o.uuid id "

	    + " , p.label "
	    + " , p.label_uc "
	    + " , p.snils "

	    + " , b.label    address"
	    + " , b.label_uc address_uc"

	    + " FROM " + CitizenCompensation.TABLE_NAME + " o"

            + " INNER JOIN " + VocPerson.TABLE_NAME + " p ON   p.uuid = o." + CitizenCompensation.c.UUID_PERSON

	    + " INNER JOIN " + VocBuilding.TABLE_NAME + " b ON   b.houseguid = o." + CitizenCompensation.c.FIASHOUSEGUID

        ;

    }

}