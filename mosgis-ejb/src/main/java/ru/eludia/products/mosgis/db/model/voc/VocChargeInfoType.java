package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Table;

public class VocChargeInfoType extends Table {
    
    public static final String TABLE_NAME = "vc_charge_types";
    
    public VocChargeInfoType () {       
        super (TABLE_NAME, "Типы строк начислений в платёжных документах");
        cols  (c.class);        
        pk    (c.ID);
        data  (i.class);
    }
    
    public enum c implements ColEnum {        
        ID         (Type.NUMERIC, 1, "Идентификатор"),
        LABEL      (Type.STRING,     "Наименование");        
                                                                                    @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);}
    }
    
    public enum i {
        
        HOUSING    (1, "Жилищная услуга"),
        MUNICIPAL  (2, "Главная коммунальная услуга с объемом потребления"),
        ADDITIONAL (3, "Вид дополнительной услуги"),
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
        
        @Override
        public String toString () {
            return Integer.toString (id);
        }
        
        public static i forId (Object iD) {
            int id = Integer.parseInt (iD.toString ());
            for (i i: values ()) if (id == i.id) return i;
            throw new IllegalArgumentException ("Invalid VocXLFileType id: " + iD);
        }
        
    }
        
}