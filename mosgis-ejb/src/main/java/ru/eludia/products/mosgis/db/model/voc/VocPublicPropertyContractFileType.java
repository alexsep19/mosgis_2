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
import ru.eludia.base.model.def.Def;
import ru.eludia.base.model.def.Num;

public class VocPublicPropertyContractFileType extends Table {
    
    private static final String TABLE_NAME = "vc_pp_ctr_file_types";
    
    public VocPublicPropertyContractFileType () {       
        super (TABLE_NAME, "Типы документов, приложенных к ДПОИ");
        cols  (c.class);        
        pk    (c.ID);
        data  (i.class);
    }
    
    public enum c implements ColEnum {        
        ID         (Type.NUMERIC, 1, "Идентификатор"),
        LABEL      (Type.STRING,     "Наименование");        
                                                                                    @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);}
    }
    
    final static Num def = new Num (i.CONTRACT.getId ());
    public static Def getDefault () {
        return def;
    }
    
    public enum i {

        CONTRACT     (1, "Договор"),
        ADDENDUM     (2, "Дополнительное соглашение"),
        VOTING_PROTO (3, "Протокол собрания собственников")
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
        
        public static i forId (long id) {
            for (i i: values ()) if (i.id == id) return i;
            return null;
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
    public  static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }
    
}