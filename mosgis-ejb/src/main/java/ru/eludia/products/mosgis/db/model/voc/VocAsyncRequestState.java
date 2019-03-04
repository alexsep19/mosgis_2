package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocAsyncRequestState extends Table {

    private static JsonArray jsonArray;
    
    private static final String TABLE_NAME = "vc_async_request_states";
    
    public static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }     
    
    static {
        
        JsonArrayBuilder builder = Json.createArrayBuilder ();
        
        for (VocAsyncRequestState.i value: VocAsyncRequestState.i.values ()) builder.add (Json.createObjectBuilder ()
            .add ("id",    value.id)
            .add ("label", value.label)
        );
                    
        jsonArray = builder.build ();
        
    }
    
    
    public VocAsyncRequestState () {
        
        super (TABLE_NAME, "Статусы обработки запросов");
        
        pk    ("id",           Type.INTEGER, "Ключ");        
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);

    }
    
    public enum i {
                
        ACCEPTED    (1, "получено"),
        IN_PROGRESS (2, "в обработке"),
        DONE        (3, "обработано");
        
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
        
        public static i forId (byte id) {
            for (i i: values ()) if (i.id == id) return i;
            return null;
        }
        
        public static i forId(Object id) {
            return forId(Byte.parseByte(id.toString()));
        }
        
    }
    
}