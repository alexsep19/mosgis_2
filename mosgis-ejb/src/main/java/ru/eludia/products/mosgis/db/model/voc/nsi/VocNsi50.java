package ru.eludia.products.mosgis.db.model.voc.nsi;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocNsi50 extends Table {
    
    public enum c implements ColEnum {
        
        CODE         (Type.STRING,  20, "Код элемента справочника, уникальный в пределах справочника"),        
        F_C6E5A29665 (Type.STRING,      "Единица измерения"),
        F_C5BA794FD6 (Type.STRING,      "Вид жилищной  услуги"),
        GUID         (Type.UUID,        "Глобально-уникальный идентификатор элемента справочника"),        
        ISACTUAL     (Type.BOOLEAN,     "Актуально")
        ;
        
        @Override public Col getCol() {return col;} private Col col; private c (Type type, Object... p) {col = new Col(this, type, p);}
        
    }
    
    public VocNsi50 () {    
        super ("vc_nsi_50", "Вид жилищной услуги");
        cols  (c.class);        
        pk    (c.GUID);
    }

}