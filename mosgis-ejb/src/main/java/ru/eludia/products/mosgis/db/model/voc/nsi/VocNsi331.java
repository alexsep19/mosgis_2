package ru.eludia.products.mosgis.db.model.voc.nsi;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocNsi331 extends Table {
    
    public enum c implements ColEnum {
        
        CODE         (Type.STRING,  20, "Код элемента справочника, уникальный в пределах справочника"),        
        F_B4AE364F08 (Type.STRING,      "Составляющая стоимости электрической энергии"),
        GUID         (Type.UUID,        "Глобально-уникальный идентификатор элемента справочника"),        
        ISACTUAL     (Type.BOOLEAN,     "Актуально")
        ;
        
        @Override public Col getCol() {return col;} private Col col; private c (Type type, Object... p) {col = new Col(this, type, p);}
        
    }
    
    public VocNsi331 () {    
        super ("vc_nsi_331", "Составляющая стоимости электрической энергии");
        cols  (c.class);        
        pk    (c.GUID);
    }

}
