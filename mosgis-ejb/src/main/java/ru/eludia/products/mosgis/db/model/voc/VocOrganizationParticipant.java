package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

public class VocOrganizationParticipant extends Table {

    private static final String TABLE_NAME = "vc_org_prtcps";

    public VocOrganizationParticipant () {

        super (TABLE_NAME, "Участие в совете правления, ревизионной комиссии товарищества, кооператива");

        pk    ("id",           Type.INTEGER, "Ключ");
        col   ("label",        Type.STRING,  "Наименование");

        data  (i.class);

    }

    private static JsonArray jsonArray;

    static {

        JsonArrayBuilder builder = Json.createArrayBuilder();

        for (i value : i.values()) {
            builder.add(Json.createObjectBuilder()
                    .add("id", value.id)
                    .add("label", value.label)
            );
        }

        jsonArray = builder.build();

    }

    public static final void addTo(JsonObjectBuilder job) {
        job.add(TABLE_NAME, jsonArray);
    }

    public enum i {
        NOT_PARTICIPANT     (10, "Не включен в состав правления/ревизионной комиссии товарищества, кооператива"),
        IN_BOARD            (20, "Избран в состав правления товарищества, кооператива"),
        IN_COMISSION        (30, "Избран в состав ревизионной комиссии товарищества, кооператива"),
        IN_COMMISSION_ALIEN (35, "Избран в состав ревизионной комиссии и не является членом товарищества, кооператива"),
        ;

        byte id;
        String label;

        public byte getId () {
            return id;
        }

        public String getLabel () {
            return label;
        }

        private i (int id, String label) {
            this.id = (byte) id;
            this.label = label;
        }
    }
}