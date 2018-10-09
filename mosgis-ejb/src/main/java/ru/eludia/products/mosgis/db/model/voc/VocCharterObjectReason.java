package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocCharterObjectReason extends Table {

    public VocCharterObjectReason () {
        
        super ("vc_charter_object_reasons", "Основания для объектов уставов");
        
        pk    ("id",           Type.INTEGER, "Ключ");        
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);

    }
    
    public enum i {

        CHARTER     (1, "Текущий устав"),
        PROTOCOL    (2, "Протокол собрания");
        
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
        
    }
    
}