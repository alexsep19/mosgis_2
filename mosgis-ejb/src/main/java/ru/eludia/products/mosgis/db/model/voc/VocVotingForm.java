package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import ru.eludia.base.model.Type;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Table;

public class VocVotingForm extends Table {
    
    private static JsonArray jsonArray;
    
    static {
        
        JsonArrayBuilder builder = Json.createArrayBuilder ();
        
        for (i value: i.values ()) builder.add (Json.createObjectBuilder ()
            .add ("id",    value.name)
            .add ("label", value.label)
        );
                    
        jsonArray = builder.build ();
        
    }
    
    private static final String TABLE_NAME = "vc_voting_forms";
    
    public static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }   

    public VocVotingForm () {
        
        super (TABLE_NAME, "Формы проведения ОСС");
        
        pk    ("name",         Type.STRING,  "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);
                
    }
    
    public enum i {
        
        AVOTING    ("avoting", "Заочное голосование (опросным путем)"),
        MEETING    ("meeting", "Очное голосование"),
        EVOTING    ("evoting", "Заочное голосование с использованием системы"),
        MEET_AV    ("meet_av", "Очно-заочное голосование");
                
        String name;
        String label;
        
        public String getName () {
            return name;
        }

        public String getLabel () {
            return label;
        }

        private i (String name, String label) {
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
