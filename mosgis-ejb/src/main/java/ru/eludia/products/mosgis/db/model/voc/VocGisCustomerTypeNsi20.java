package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.products.mosgis.db.model.voc.VocGisCustomerType.i.*;

public class VocGisCustomerTypeNsi20 extends Table {

    public VocGisCustomerTypeNsi20 () {

        super  ("vc_gis_customer_type_nsi_20",  "Полномочия по тиам контрагентов");
        
        pkref  ("id", VocGisCustomerType.class, "Тип");
        pk     ("code", Type.STRING, 20, "Код полномочия (НСИ 20)");
        
        data (i.class);
       
    }
    
    public enum i {
        
        BO      (BUILDINGOWNER,     "22"),
        MUNI_7  (MUNICIPAL_HOUSING, "7"),
        MUNI_8  (MUNICIPAL_HOUSING, "8"),
        COOP_19 (COOPERATIVE,       "19"),
        COOP_21 (COOPERATIVE,       "21");

        byte id;
        String code;

        public byte getId () {
            return id;
        }

        public String getCode () {
            return code;
        }

        private i (VocGisCustomerType.i i, String code) {
            this.id = i.getId ();
            this.code = code;
        }
        
    }

}