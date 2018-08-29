package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.products.mosgis.db.model.voc.VocGisCustomerType.i.*;

public class VocGisCustomerTypeNsi58 extends Table {

    public VocGisCustomerTypeNsi58 () {

        super  ("vc_gis_customer_type_nsi_58",  "Основания заключения по типам заказчиков договоров управления");
        
        pk     ("id",         Type.INTEGER, "Тип заказчика договора");        
        pk     ("code",       Type.STRING,  20, "Основание заключения (НСИ 58)");
        col    ("isdefault",  Type.BOOLEAN, Bool.FALSE, "1 для значений по умолчанию, 0 для остальных");
        
        data (i.class);
       
    }
    
    public enum i {
        
        BO_1      (BUILDINGOWNER,     "1"),
        BO_2      (BUILDINGOWNER,     "2"),
        BO_6      (BUILDINGOWNER,     "6", true),
        BO_9      (BUILDINGOWNER,     "9"),
        BO_10     (BUILDINGOWNER,     "10"),
        
        OWNERS_1  (OWNERS,            "1", true),
        OWNERS_2  (OWNERS,            "2"),

        MUNI_1    (MUNICIPAL_HOUSING, "1"),
        MUNI_2    (MUNICIPAL_HOUSING, "2", true),
        
        COOP_1    (COOPERATIVE,        "1"),
        COOP_2    (COOPERATIVE,        "2", true),
        COOP_5    (COOPERATIVE,        "5");

        byte id;
        String code;
        byte isdefault = 0;

        public byte getId () {
            return id;
        }

        public String getCode () {
            return code;
        }

        public byte getIsdefault () {
            return isdefault;
        }

        private i (VocGisCustomerType.i i, String code) {
            this.id   = i.getId ();
            this.code = code;
        }
        
        private i (VocGisCustomerType.i i, String code, boolean d) {
            this.id        = i.getId ();
            this.code      = code;
            if (d) this.isdefault = (byte) 1;
        }

    }

}