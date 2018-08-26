package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocNsiListGroup extends Table {

    public VocNsiListGroup () {
        
        super ("vc_nsi_list_group", "Группы справочников НСИ");
        
        pk    ("name",         Type.STRING,  "Символическое имя");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);

    }
    
    public enum i {
                        
        NSI    ("NSI", "Общесистемные"),
        NSIRAO ("NSIRAO", "ОЖФ");
        
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
        
        public final static i forName (String name) {
            
            for (i i: values ()) if (i.getName ().equals (name)) return i;
            
            return null;
            
        }
        
    }
    
}