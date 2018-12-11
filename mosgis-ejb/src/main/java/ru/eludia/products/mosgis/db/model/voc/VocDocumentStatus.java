package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class VocDocumentStatus extends Table {
    
    private static final String TABLE_NAME = "vc_document_status";

    public VocDocumentStatus () {

        super (TABLE_NAME, "Статусы лицензий");

        pk    ("id",           Type.INTEGER, "Ключ");        
        col   ("name",         Type.STRING,  "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);
        
    }
    
    public enum i {

        ACTIVE      (10,  "A", "Действующий"),
        CANCELED    (20,  "C", "Отменен"),
        PROJECT     (30,  "P", "Проект");

        byte id;
        String name;
        String label;
        
        public byte getId () {
            return id;
        }

        public String getName () {
            return name;
        }

        public String getLabel () {
            return label;
        }

        private i (int id, String name, String label) {
            this.id = (byte) id;
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
