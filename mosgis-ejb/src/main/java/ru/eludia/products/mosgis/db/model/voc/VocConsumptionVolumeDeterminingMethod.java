package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Table;

public class VocConsumptionVolumeDeterminingMethod extends Table {
    
    public static final String TABLE_NAME = "vc_cnsmp_vol_dtrm";
    
    public VocConsumptionVolumeDeterminingMethod () {       
        super (TABLE_NAME, "Способы определения объёмов потребления коммунальных услуг");
        cols  (c.class);        
        pk    (c.ID);
        data  (i.class);
    }
    
    public enum c implements ColEnum {        
        ID         (Type.STRING, 1, "Идентификатор"),
        LABEL      (Type.STRING,    "Наименование");        
                                                                                    @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);}
    }
    
    public enum i {
        
        NORM            ("N", "Норматив"),
        METERING_DEVICE ("M", "Прибор учёта"),
        OTHER           ("O", "Иное"),
        ;
                
        String id;
        String label;

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
        
        @Override
        public String toString () {
            return id;
        }
        
        public static i forId (Object iD) {
            for (i i: values ()) if (i.id.equals (iD)) return i;
            throw new IllegalArgumentException ("Invalid VocConsumptionVolumeDeterminingMethod id: " + iD);
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
        
    }

    private static JsonArray jsonArray = i.toJsonArray ();    

    public static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }

}