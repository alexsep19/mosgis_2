package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.def.Num;

public class VocGisContractDimension extends Table {

    private static JsonArray jsonArray;
    private static JsonArray jsonArrayLite;

    private static final String TABLE_NAME = "vc_gis_ctr_dims";

    public static final Num DEFAULT = new Num (VocGisStatus.i.PROJECT.getId ());

    public static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }

    public static final void addLiteTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArrayLite);
    }

    static {

        JsonArrayBuilder builder = Json.createArrayBuilder ();
        JsonArrayBuilder builderLite = Json.createArrayBuilder ();

        for (i value: i.values ()) {

            builder.add (Json.createObjectBuilder ()
                .add ("id",    value.id)
                .add ("label", value.label)
            );

            builderLite.add (Json.createObjectBuilder ()
                .add ("id",    value.id)
                .add ("label", value.label)
            );

        }

        jsonArray = builder.build ();
        jsonArrayLite = builderLite.build ();

    }

    public VocGisContractDimension () {

        super (TABLE_NAME, "Статусы процессов утверждения в ГИС ЖКХ (из hcs-house-management-types.xsd + собственные)");

        pk    ("id",           Type.INTEGER, "Ключ");
        col   ("name",         Type.STRING,  "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");

        data  (i.class);

    }

    public enum i {

        BY_CONTRACT              (10, "D", "в разрезе договора"),
        BY_HOUSE                 (20, "O", "в разрезе объектов жилищного фонда"),
        ;

        byte id;
        String name;
        String label;

        public ru.eludia.base.model.def.Num asDef () {
            return new ru.eludia.base.model.def.Num (id);
        }

        public byte getId () {
            return id;
        }

        public String getName () {
            return name;
        }

        public String getLabel () {
            return label;
        }

        private i (int id, String name, String label) {
            this.id = (byte) id;
            this.name = name;
            this.label = label;
        }

        public static i forName (String name) {
            for (i i: values ()) if (i.name.equals (name)) return i;
            return null;
        }

        public static i forId (int id) {
            for (i i: values ()) if (i.id == id) return i;
            return null;
        }

        public static i forId (Object id) {
            return forId (Integer.parseInt (id.toString ()));
        }

        @Override
        public String toString () {
            return Byte.toString (id);
        }

    }

}