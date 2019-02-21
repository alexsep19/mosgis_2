package ru.eludia.products.mosgis.db.model.voc.nsi;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocNsi16 extends Table {
    
    public enum c implements ColEnum {
        
        CODE         (Type.STRING,  20, "Код элемента справочника, уникальный в пределах справочника"),        
        F_A008C7D1F3 (Type.STRING,      "Единица измерения интервала"),
        F_17E03CCF84 (Type.NUMERIC, 10, "Межповерочный интервал"),                
        GUID         (Type.UUID,        "Глобально-уникальный идентификатор элемента справочника"),        
        ISACTUAL     (Type.BOOLEAN,     "Актуально")
        ;
        
        @Override public Col getCol() {return col;} private Col col; private c (Type type, Object... p) {col = new Col(this, type, p);}
        
    }
    
    public VocNsi16 () {
        super ("vc_nsi_16", "Межповерочный интервал");
        cols  (c.class);        
    }

}
