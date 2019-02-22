package ru.eludia.products.mosgis.db.model.voc.nsi;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocNsi27 extends Table {
    
    public enum c implements ColEnum {
        
        CODE         (Type.STRING,  20, "Код элемента справочника, уникальный в пределах справочника"),        
        F_BCAB5CAF38 (Type.STRING,      "Тип прибора учета"),
        GUID         (Type.UUID,        "Глобально-уникальный идентификатор элемента справочника"),        
        ISACTUAL     (Type.BOOLEAN,     "Актуально")
        ;
        
        @Override public Col getCol() {return col;} private Col col; private c (Type type, Object... p) {col = new Col(this, type, p);}
        
    }
    
    public VocNsi27 () {    
        super ("vc_nsi_27", "Тип прибора учета");
        cols  (c.class);        
        pk    (c.GUID);
    }

}
