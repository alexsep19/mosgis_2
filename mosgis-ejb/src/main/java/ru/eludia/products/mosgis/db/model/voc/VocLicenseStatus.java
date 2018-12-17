package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;

public class VocLicenseStatus extends Table {
    
    private static final String TABLE_NAME = "vc_license_status";

    public VocLicenseStatus () {
        super   (TABLE_NAME, "Статусы лицензий");
        cols    (c.class);
        pk      (c.ID);
        data    (i.class);
    }
    
    public enum c implements ColEnum {        
        ID         (Type.NUMERIC, 2, "Идентификатор"),
        LABEL      (Type.STRING,     "Наименование"),        
        NAME       (Type.STRING,     "Имя");      
        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);}
    }        
    
    public enum i {

        ACTIVE      (10,  "включена в реестр, действующая",                                                             "A"),
        FINISHED    (20,  "включена в реестр, действие прекращено в зависимости от даты окончания действия лицензии",   "F"),
        CANCELED    (30,  "аннулирована",                                                                               "C"),
        REJECTED    (40,  "отменена в зависимости от основания",                                                        "R"),
        INACTIVE    (50,  "не включена в реестр, не действующая",                                                       "I");

        int id;
        String label;
        String name;
        
        public ru.eludia.base.model.def.Num asDef () {
            return new ru.eludia.base.model.def.Num (id);
        }        
        
        public int getId () {
            return id;
        }

        public String getName () {
            return name;
        }

        public String getLabel () {
            return label;
        }

        private i (int id, String label, String name) {
            this.id = id;
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
