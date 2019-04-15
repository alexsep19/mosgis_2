package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import ru.eludia.base.model.Type;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;

public class VocServiceType extends Table {

    private static final String TABLE_NAME = "vc_svc_types";

    public VocServiceType () {
        super (TABLE_NAME, "Тип услуг");
        cols  (c.class);
        pk    (c.ID);
        data  (i.class);
    }

    public enum c implements ColEnum {

        ID    (Type.STRING, "Идентификатор"),
        LABEL (Type.STRING, "Наименование"),
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public enum i {
	COLDWATER             ("ColdWater", "Холодное водоснабжение"),
	HOTWATER              ("HotWater", "Горячее водоснабжение"),
	SEWERAGE              ("Sewerage", "Отведение сточных вод"),
	ELECTRICITY           ("Electricity", "Электроснабжение"),
	GAS                   ("Gas", "Газоснабжение"),
	SOLIDFUEL             ("SolidFuel", "Твердое топливо"),
	LIQUIDGAS             ("LiquidGas", "Газ в баллонах"),
	HEATING               ("Heating", "Отопление"),
	WASTE                 ("Waste", "Обращение с твердыми коммунальными отходами"),
	MAINTENANCE           ("Maintenance", "Содержание жилого помещения"),
	REPAIRS               ("Repairs", "Взносы на капитальный ремонт"),
	RENT                  ("Rent", "Наем жилого помещения"),
	COLLECTIVEHOTWATER    ("CollectiveHotWater", "Горячая вода на содержание общего имущества"),
	COLLECTIVECOLDWATER   ("CollectiveColdWater", "Холодная вода на содержание общего имущества"),
	COLLECTIVEELECTRICITY ("CollectiveElectricity", "Электрическая энергия на содержание общего имущества"),
	COLLECTIVESEWERAGE    ("CollectiveSewerage", "Отведение сточных вод на содержание общего имущества")
        ;

        String  id;
        String  label;

        public String getId () {
            return id;
        }

        public String getLabel () {
            return label;
        }

        private i (String id, String label) {
            this.id = id;
            this.label = label;
        }

        public static i forId (Object id) {
            for (i i: values ()) if (DB.eq (id, i.id)) return i;
            throw new IllegalArgumentException ("Unknown VocServiceType id: " + id);
        }

        @Override
        public String toString () {
            return id;
        }

        private JsonObject toJsonObject () {

            final JsonObjectBuilder job = Json.createObjectBuilder ()
                .add ("id",    id)
                .add ("label", label)
            ;

            return job.build ();

        }

        public static JsonArray toJsonArray () {
            JsonArrayBuilder builder = Json.createArrayBuilder ();
            for (i value: i.values ()) builder.add (value.toJsonObject ());
            return builder.build ();
        }

    }

    private static JsonArray jsonArray = i.toJsonArray ();

    public static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }

}