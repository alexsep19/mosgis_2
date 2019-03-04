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
import ru.eludia.base.model.def.Num;

public class VocMeteringDeviceValueType extends Table {
    
    private static final String TABLE_NAME = "vc_meter_value_types";
    public  static final Num    DEFAULT    = new Num (i.CURRENT.getId ());
    
    public VocMeteringDeviceValueType () {       
        super (TABLE_NAME, "Типы показаний приборов учёта");
        cols  (c.class);        
        pk    (c.ID);
        data  (i.class);
    }
    
    public enum c implements ColEnum {        
        
        ID    (Type.NUMERIC, 1, "Идентификатор"),
        LABEL (Type.STRING,     "Наименование"),
        ;
        
        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
        
    }
    
    public enum i {

        CURRENT (1, "Текущее показание"),
        CONTROL (2, "Контрольное показание"),
        BASE    (3, "Базовое показание"),
        ;
        
        int     id;
        String  label;

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
        
        public static i forId (Object id) {
            for (i i: values ()) if (DB.eq (id, i.id)) return i;
            throw new IllegalArgumentException ("Unknown type id: " + id);
        }

        @Override
        public String toString () {
            return Integer.toString (id);
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
        
        public static JsonArray toJsonArrayVolume () {            
            JsonArrayBuilder builder = Json.createArrayBuilder ();                    
            for (i i: i.values ()) if (i != i.BASE) builder.add (i.toJsonObject ());            
            return builder.build ();
        }
        
    }
    
    private static JsonArray jsonArray = i.toJsonArray ();    
    private static JsonArray jsonArrayVolume = i.toJsonArrayVolume ();    
    
    public static final void addTo (JsonObjectBuilder job, boolean isVolume) {
        job.add (TABLE_NAME, isVolume ? jsonArrayVolume : jsonArray);
    }
    
}