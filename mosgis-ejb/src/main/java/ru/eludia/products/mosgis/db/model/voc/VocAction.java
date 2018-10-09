package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

public class VocAction extends Table {

    private static JsonArray jsonArray;
    
    public VocAction () {
        
        super ("vc_actions", "Действия пользователей системы");
        
        pk    ("name",         Type.STRING,  "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);
        
        JsonArrayBuilder builder = Json.createArrayBuilder ();
        
        for (i value: i.values ()) {
            
            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder ();
            jsonBuilder.add("id", value.name).add ("label", value.label);
            builder.add (jsonBuilder.build ());
            
        }
        
        jsonArray = builder.build ();
        
    }
    
    public enum i {
        
        CREATE    ("create",    "создание"),
        UPDATE    ("update",    "редактирование"),
        DELETE    ("delete",    "удаление"),
        UNDELETE  ("undelete",  "восстановление"),
        APPROVE   ("approve",   "утверждение"),
        TERMINATE ("terminate", "расторжение"),
        ALTER     ("alter",     "изменение"),
        ANNUL     ("annul",     "аннулирование"),
        ROLLOVER  ("rollover",  "пролонгирование"),
        PROMOTE   ("promote",   "утверждение"),
        REFRESH   ("refresh",   "обновление");
                
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

    public static JsonArray getVocJson () {
        return jsonArray;
    }
    
}