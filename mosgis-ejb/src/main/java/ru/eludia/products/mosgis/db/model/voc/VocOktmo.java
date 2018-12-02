package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;

public class VocOktmo extends Table {
    
    public enum c implements EnColEnum {
        
        AREA_CODE       (Type.STRING,   3,       "Код района/города МО"),
        SETTLEMENT_CODE (Type.STRING,   3,       "Код поселения МО"),
        LOCALITY_CODE   (Type.STRING,   3,       "Код населенного пункта МО"),
        CONTROL_NUM     (Type.NUMERIC,  1,       "Контрольное число"),
        SECTION_CODE    (Type.STRING,   1,       "Код раздела"),
        SITE_NAME       (Type.STRING, 500,       "Наименование территории"),
        ADD_INFO        (Type.STRING, 250, null, "Дополнительная информация"),
        DESCRIPTION     (Type.STRING,      null, "Описание"),
        AKT_NUM         (Type.NUMERIC,  3,       "Номер изменения"),
        STATUS          (Type.NUMERIC,  1,       "Тип изменения"),
        APPR_DATE       (Type.DATE,              "Дата утверждения"),
        ADOP_DATE       (Type.DATE,              "Дата принятия"),
        
        CODE            (Type.STRING,  11, new Virt ("DECODE(\"LOCALITY_CODE\", '000', ('45' || \"AREA_CODE\" || \"SETTLEMENT_CODE\"), "
                                                                                    + "('45' || \"AREA_CODE\" || \"SETTLEMENT_CODE\" || \"LOCALITY_CODE\"))"), "Код ОКТМО")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            return false;
        }

    }
    
    public static String[] fieldNames;
    
    public VocOktmo () {
        
        super ("vc_oktmo", "Общероссийский классификатор территорий муниципальных образований");
        
        cols (c.class);
        
        pk (c.AREA_CODE);
        pk (c.SETTLEMENT_CODE);
        pk (c.LOCALITY_CODE);
        pk (c.SECTION_CODE);
        
        key ("aread_code", "area_code");
        key ("settlement_code", "settlement_code");
        key ("locality_code", "locality_code");
        key ("section_code", "section_code");
        
        fieldNames = new String[c.values ().length];
        for (int i = 0; i < fieldNames.length; i++)
            fieldNames[i] = c.values ()[i].lc ();
        
    }
    
}
