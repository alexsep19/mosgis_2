package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import ru.eludia.base.model.Type;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Table;

public class VocDelegationStatus extends Table {
    
    private static JsonArray jsonArray;
    
    static {
        
        JsonArrayBuilder builder = Json.createArrayBuilder ();
        
        for (i value: i.values ()) builder.add (Json.createObjectBuilder ()
            .add ("id",    value.id)
            .add ("label", value.label)
        );
                    
        jsonArray = builder.build ();
        
    }
    
    private static final String TABLE_NAME = "vc_delegation_status";
    
    public static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }   

    public VocDelegationStatus () {
        
        super (TABLE_NAME, "Статусы делегирования прав организаций");
        
        pk    ("id",           Type.NUMERIC, 3, "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);
                
    }
    
    public enum i {

        EXPIRED (-1, "Были"),
        NO      ( 0, "Нет"),
        PRESENT ( 1, "Есть"),
        ;

        int    id;
        String label;

        public String getLabel () {
            return label;
        }

        public int getId () {
            return id;
        }

        private i (int id, String label) {
            this.id = id;
            this.label = label;            
        }


        @Override
        public String toString () {
            return Integer.toString (id);
        }

    }        
    
}
