package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.def.Num;

public class VocHouseStatus extends Table {
    
    private static JsonArray jsonArray;
    
    private static final String TABLE_NAME = "vc_house_status";
    
    public static final Num DEFAULT = new Num (VocHouseStatus.i.MISSING.getId ());
    
    public static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }       
    
    static {
        
        JsonArrayBuilder builder = Json.createArrayBuilder ();
        
        for (i value: i.values ()) builder.add (Json.createObjectBuilder ()
            .add ("id",    value.id)
            .add ("label", value.label)
        );
                    
        jsonArray = builder.build ();
        
    }    

    public VocHouseStatus () {
        
        super (TABLE_NAME, "Статусы ГИС ЖКХ объекта жилищного фонда");
        
        pk    ("id",           Type.INTEGER, "Ключ");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);

    }
    
    public enum i {

        MISSING        (10, "Отсутствует"),
        PUBLISHED      (20, "Опубликован"),
        PUBLISHED_PART (30, "Опубликован частично");

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

        public static i forId (int id) {
            for (i i: values ()) if (i.id == id) return i;
            return null;
        }

        public static i forId (Object id) {
            return forId (Integer.parseInt (id.toString ()));
        }

    }

}