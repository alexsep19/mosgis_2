package ru.eludia.products.mosgis.db.model.voc.nsi;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;

public class Nsi301 extends View {

    public enum c implements ColEnum {

        ID     (Type.STRING, 20, null, "Код"),
        LABEL  (Type.STRING,     null, "Наименование"),
	GUID   (Type.UUID,       null, "Глобально-уникальный идентификатор элемента справочника"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public Nsi301 () {
        super  ("vw_nsi_301", "Типы решений о мерах социальной поддержки");
        cols   (c.class);
        pk     (c.ID);
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " code id, "
            + VocNsi301.c.F_C55410BED7.name () + " label "
	    + " , guid "
            + "FROM "
            + " vc_nsi_301 "
            + "WHERE"
            + " isactual=1"
        ;

    }

    public enum i {

	PROVISION    (1, "О предоставлении"),
	SUSPENSION   (2, "О приостановлении"),
	RENEWAL      (3, "О возобновлении"),
	TERMINATION  (4, "О прекращении предоставления")
        ;

        int    id;
        String label;

        public int getId () {
            return id;
        }

        public String getLabel () {
            return label;
        }

        private i (int id, String label) {
            this.id = id;
            this.label = label;
        }

        private JsonObject toJsonObject () {
            return Json.createObjectBuilder ()
                .add ("id",    id)
                .add ("label", label)
            .build ();
        }

        public static JsonArray toJsonArray () {
            JsonArrayBuilder builder = Json.createArrayBuilder ();
            for (i value: i.values ()) builder.add (value.toJsonObject ());
            return builder.build ();
        }

        private static JsonArray jsonArray = i.toJsonArray ();

        public  static final void addTo (JsonObjectBuilder job) {
            job.add ("vc_nsi_301", jsonArray);
        }
    }

}