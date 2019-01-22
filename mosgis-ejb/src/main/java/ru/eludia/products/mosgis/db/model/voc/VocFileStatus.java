package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import ru.eludia.base.model.Type;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.def.Num;

public class VocFileStatus extends Table {
    
    private static final String TABLE_NAME = "vc_file_status";
    
    public static final Num DEFAULT = new Num (VocFileStatus.i.LOADING.getId ());

    public VocFileStatus () {       
        super (TABLE_NAME, "Статусы загружаемых файлов");
        cols  (c.class);        
        pk    (c.ID);
        data  (i.class);
    }
    
    public enum c implements ColEnum {        
        ID         (Type.NUMERIC, 1, "Идентификатор"),
        LABEL      (Type.STRING,     "Наименование");        
                                                                                    @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);}
    }
    
    public enum i {
        
        LOADING    (0, "В процессе загрузки"),
        LOADED     (1, "Загружен"),
        DELETED    (2, "Удалён"),
        PROCESSING (3, "В обработке"),
        PROCESSED  (4, "Обработан"),
        ;
                
        int    id;
        String label;

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

        @Override
        public String toString () {
            return Integer.toString (id);
        }
        
        public static i forId (Object iD) {
            int id = Integer.parseInt (iD.toString ());
            for (i i: values ()) if (id == i.id) return i;
            throw new IllegalArgumentException ("Invalid VocFileStatus id: " + iD);
        }

    }
    
    private static JsonArray jsonArray = i.toJsonArray ();    
    public  static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }
    
}