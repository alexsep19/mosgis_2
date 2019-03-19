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
import ru.eludia.products.mosgis.db.model.voc.VocLegalActLevel;

public class Nsi324 extends View {

    public enum c implements ColEnum {

        ID     (Type.STRING, 20, null, "Код"),
        LABEL  (Type.STRING,     null, "Наименование"),
	LEVEL_ (VocLegalActLevel.class, null, "Уровень (сфера действия)")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public Nsi324 () {
        super  ("vw_nsi_324", "Вид закона и нормативного акта");
        cols   (c.class);
        pk     (c.ID);
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " code id "
	    + " , F_BCE3C198BF label "
	    + " , CASE WHEN " + VocNsi324.c.F_6C1D96410E + " = 'FEDERAL' THEN " + VocLegalActLevel.i.FEDERAL
	    + "    WHEN " + VocNsi324.c.F_6C1D96410E + " = 'REGIONAL' THEN " + VocLegalActLevel.i.REGIONAL
	    + "    WHEN " + VocNsi324.c.F_6C1D96410E + " = 'MUNICIPAL' THEN " + VocLegalActLevel.i.MUNICIPAL
	    + "  END level_ "
            + "FROM "
            + " vc_nsi_324 "
            + "WHERE"
            + " isactual=1"
        ;

    }

    public static Select select() {

	final MosGisModel m = ModelHolder.getModel();

	return m.select(Nsi324.class, "AS root", "*")
	    .orderBy(Nsi324.c.ID);
    }

    public static void addTo(JsonObjectBuilder job, DB db) throws SQLException {
	job.add("vc_nsi_324", db.getJsonArray(select()));
    }
}