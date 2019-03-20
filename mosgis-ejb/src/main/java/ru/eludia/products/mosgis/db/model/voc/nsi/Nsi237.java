package ru.eludia.products.mosgis.db.model.voc.nsi;

import java.sql.SQLException;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;

public class Nsi237 extends View {

    public enum c implements ColEnum {

        ID     (Type.STRING, 20, null, "Код"),
        LABEL  (Type.STRING,     null, "Наименование"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public static String CODE_MOSCOW = "77";

    public Nsi237 () {
        super  ("vw_nsi_237", "Коды субъектов Российской Федерации (регионов)");
        cols   (c.class);
        pk     (c.ID);
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " code id, "
            + VocNsi237.c.F_64B1C12EF9.lc () + " || ' ' || " + VocNsi237.c.f_356213bef6.lc () + " label "
            + "FROM "
            + " vc_nsi_237 "
            + "WHERE"
            + " isactual=1"
        ;

    }

    public static Select select() {

	final MosGisModel m = ModelHolder.getModel();

	return m.select(Nsi237.class, "AS root", "*")
	    .orderBy(Nsi237.c.LABEL);
    }

    public static void addTo(JsonObjectBuilder job, DB db) throws SQLException {
	job.add("vc_nsi_237", db.getJsonArray(select()));
    }
}