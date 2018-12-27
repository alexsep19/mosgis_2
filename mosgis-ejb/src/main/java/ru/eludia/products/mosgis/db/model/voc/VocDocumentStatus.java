package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class VocDocumentStatus extends Table {
    
    private static JsonArray jsonArray;
    
    private static final String TABLE_NAME = "vc_document_status";

    public static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }
    
    
    static {
        
        JsonArrayBuilder builder = Json.createArrayBuilder ();
        
        for (VocDocumentStatus.i value: VocDocumentStatus.i.values ()) {
            
            builder.add (Json.createObjectBuilder ()
                .add ("id",    value.id)
                .add ("label", value.label)
            );
            
        }
                    
        jsonArray = builder.build ();
        
    } 
    
    
    public VocDocumentStatus () {
        super   (TABLE_NAME, "Статусы лицензий");
        cols    (c.class);
        pk      (c.ID);
        data    (i.class);
    }
    
    public enum c implements ColEnum {        
        ID         (Type.NUMERIC, 2, "Идентификатор"),
        LABEL      (Type.STRING,     "Наименование"),        
        NAME       (Type.STRING,     "Имя");  
        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);}
    }    
    
    public enum i {

        ACTIVE      (10,  "Действующий", "A"),
        CANCELED    (20,  "Отменен",     "C"),
        PROJECT     (30,  "Проект",      "P");

        int id;
        String label;
        String name;        
        
        public int getId () {
            return id;
        }

        public String getName () {
            return name;
        }

        public String getLabel () {
            return label;
        }

        private i (int id, String label, String name) {
            this.id = id;
            this.name = name;
            this.label = label;            
        }

        public static VocDocumentStatus.i forName (String name) {
            for (VocDocumentStatus.i i: values ()) if (i.name.equals (name)) return i;
            return null;
        }

        public static VocDocumentStatus.i forId (int id) {
            for (VocDocumentStatus.i i: values ()) if (i.id == id) return i;
            return null;
        }

        public static VocDocumentStatus.i forId (Object id) {
            return forId (Integer.parseInt (id.toString ()));
        }

    }    
    
}
