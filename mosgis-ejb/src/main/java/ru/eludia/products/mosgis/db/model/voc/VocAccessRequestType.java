package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import ru.eludia.base.model.Type;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Table;

public class VocAccessRequestType extends Table {
    
    private static JsonArray jsonArray;
    
    static {
        
        JsonArrayBuilder builder = Json.createArrayBuilder ();
        
        for (i value: i.values ()) builder.add (Json.createObjectBuilder ()
            .add ("id",    value.id)
            .add ("label", value.label)
        );
                    
        jsonArray = builder.build ();
        
    }
    
    private static final String TABLE_NAME = "vc_acc_req_types";
    
    public static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }   

    public VocAccessRequestType () {
        
        super (TABLE_NAME, "Типы заявок на предоставление доступа");
        
        pk    ("id",           Type.NUMERIC, 1, "Идентификатор");
        col   ("name",         Type.STRING,  "Значение в XML");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);
                
    }
    
    public enum i {
        
        INFO_SYS    (1, "ForInformationSystemOperator", "Для оператора информационной системы"),
        CALC_CENTER (2, "ForCalculationCenter", "Для расчетного центра");
                
        int    id;
        String name;
        String label;
        
        public String getName () {
            return name;
        }

        public String getLabel () {
            return label;
        }

        public int getId () {
            return id;
        }

        private i (int id, String name, String label) {
            this.id = id;
            this.name = name;
            this.label = label;            
        }

        public static i forName (String name) {
            for (i i: values ()) if (i.name.equals (name)) return i;
            return null;
        }

        @Override
        public String toString () {
            return name;
        }

    }
    
}
