package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.ejb.ModelHolder;

public class VocAction extends Table {

    public VocAction () {
        
        super ("vc_actions", "Действия пользователей системы");
        
        pk    ("name",         Type.STRING,  "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);

    }
    
    public enum i {
        
        CREATE   ("create",   "создание"),
        UPDATE   ("update",   "редактирование"),
        DELETE   ("delete",   "удаление"),
        UNDELETE ("undelete", "восстановление"),
        APPROVE  ("approve",  "утверждение"),
        ALTER    ("alter",    "изменение"),
        ANNUL    ("annul",    "аннулирование"),
        PROMOTE  ("promote",  "утверждение"),
        REFRESH  ("refresh",  "обновление");
                
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
    
    public static Select getVocSelect () {
        return ModelHolder.getModel ().select (VocAction.class, "name AS id", "label");        
    }

}