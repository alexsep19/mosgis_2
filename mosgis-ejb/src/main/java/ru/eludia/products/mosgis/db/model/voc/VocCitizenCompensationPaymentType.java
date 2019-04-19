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
import ru.gosuslugi.dom.schema.integration.msp.ServiceType;

public class VocCitizenCompensationPaymentType extends Table {

    private static final String TABLE_NAME = "vc_cit_comp_pay_types";

    public VocCitizenCompensationPaymentType () {
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
	PAYMENT               ("Payment", "Выплата гражданину"),
	REFUND                ("Refund", "Возврат гражданином излишне полученной суммы")
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
            throw new IllegalArgumentException ("Unknown VocCitizenCompensationPaymentType id: " + id);
        }

	public static i forId (ServiceType id) {
	    String q = id.toString().toUpperCase().replace("_", "");
            for (i i: values ()) if (DB.eq (q, i.id.toUpperCase())) return i;
            throw new IllegalArgumentException ("Unknown VocServiceType id: " + q);
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