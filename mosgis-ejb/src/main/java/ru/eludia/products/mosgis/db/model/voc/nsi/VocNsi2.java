package ru.eludia.products.mosgis.db.model.voc.nsi;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocNsi2 extends Table {
    
    public enum c implements ColEnum {
        
        CODE         (Type.STRING,  20, "Код элемента справочника, уникальный в пределах справочника"),
        F_C8D77DDAD5 (Type.STRING,      "Вид коммунального ресурса"),
        F_C6E5A29665 (Type.STRING,      "Единица измерения"),
        GUID         (Type.UUID,        "Глобально-уникальный идентификатор элемента справочника"),
        ISACTUAL     (Type.BOOLEAN,     "Актуально")
        ;
        
        @Override public Col getCol() {return col;} private Col col; private c (Type type, Object... p) {col = new Col(this, type, p);}
        
    }
    
    public VocNsi2 () {    
        super ("vc_nsi_2", "Вид коммунального ресурса");
        cols  (c.class);        
        pk    (c.GUID);
    }

}
