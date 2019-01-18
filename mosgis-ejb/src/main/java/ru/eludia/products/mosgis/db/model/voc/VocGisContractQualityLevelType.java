package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocGisContractQualityLevelType extends Table {

    private static final String TABLE_NAME = "vc_gis_sr_ql_types";

    public VocGisContractQualityLevelType () {

        super (TABLE_NAME, "Типы показателей качества в договорах ресурсоснабжения (см. НСИ 276)");

        pk    ("id",           Type.INTEGER, "Ключ");
        col   ("name",         Type.STRING,  "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");

        data  (i.class);

    }

    public enum i {

        RANGE               (1, "Range", "Диапазон"),
        NUMBER              (2, "Number", "Число"),
        CORRESPOND          (3, "Correspond", "Логическое")
        ;


        byte id;
        String name;
        String label;

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
    }

    private static JsonArray jsonArray;

    public static final void addTo(JsonObjectBuilder job) {
        job.add(TABLE_NAME, jsonArray);
    }

    static {

        JsonArrayBuilder builder = Json.createArrayBuilder();

        for (VocGisContractQualityLevelType.i value : VocGisContractQualityLevelType.i.values()) {

            builder.add(Json.createObjectBuilder()
                    .add("id", value.id)
                    .add("label", value.label)
            );
        }

        jsonArray = builder.build();
    }
}