package ru.eludia.products.mosgis.db.model.voc.nsi;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocNsi237 extends Table {
    
    public enum c implements ColEnum {
        
        CODE         (Type.STRING,  20, "Код элемента справочника, уникальный в пределах справочника"),        
        F_B1E63E0D96 (Type.STRING,      "Код субъекта"),
        F_64B1C12EF9 (Type.STRING,      "Формализованное наименование"),
        f_356213bef6 (Type.STRING,      "Краткое наименование"),
        GUID         (Type.UUID,        "Глобально-уникальный идентификатор элемента справочника"),        
        ISACTUAL     (Type.BOOLEAN,     "Актуально")
        ;
        
        @Override public Col getCol() {return col;} private Col col; private c (Type type, Object... p) {col = new Col(this, type, p);}
        
    }
    
    public VocNsi237 () {    
        super ("vc_nsi_237", "Код субъектов Российской Федерации (регионов)");
        cols  (c.class);        
        pk    (c.GUID);
    }

}
