package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class VocLicenseStatus extends Table {
    
    private static final String TABLE_NAME = "vc_license_status";

    public VocLicenseStatus () {

        super (TABLE_NAME, "Статусы лицензий");

        pk    ("id",           Type.INTEGER, "Ключ");        
        col   ("name",         Type.STRING,  "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);
        
    }
    
    public enum i {

        ACTIVE      (10,  "A", "включена в реестр, действующая"),
        FINISHED    (20,  "F", "включена в реестр, действие прекращено в зависимости от даты окончания действия лицензии"),
        CANCELED    (30,  "C", "аннулирована"),
        REJECTED    (40,  "R", "отменена в зависимости от основания"),
        INACTIVE    (50,  "I", "не включена в реестр, не действующая");
    

        byte id;
        String name;
        String label;
        
        public ru.eludia.base.model.def.Num asDef () {
            return new ru.eludia.base.model.def.Num (id);
        }        
        
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

        public static VocLicenseStatus.i forName (String name) {
            for (VocLicenseStatus.i i: values ()) if (i.name.equals (name)) return i;
            return null;
        }

        public static VocLicenseStatus.i forId (int id) {
            for (VocLicenseStatus.i i: values ()) if (i.id == id) return i;
            return null;
        }

        public static VocLicenseStatus.i forId (Object id) {
            return forId (Integer.parseInt (id.toString ()));
        }

    }    
    
}
