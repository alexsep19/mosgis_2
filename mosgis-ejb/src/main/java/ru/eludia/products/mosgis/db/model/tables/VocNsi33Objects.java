package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;

public class VocNsi33Objects extends View {
    
    public enum c implements ColEnum {
        
        UUID (Type.UUID,   null, "Ключ"),
        CODE (Type.STRING,   20, "Код элемента справочника")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
                
    }
    
    public VocNsi33Objects () {
        
        super ("vw_nsi_33_objects", "Элементы справочника НСИ 33, имеющие тип 'Объект'");
        
        cols   (c.class);
        pk     (c.UUID);
        
    }
    
    @Override
    public final String getSQL () {
        
        return ""
                + "SELECT "
                    + "nsi33.code "
                + "FROM "
                    + "vc_nsi_33 nsi33 "
                + "WHERE "
                    + "nsi33.type='Объект'"
                
        ;
        
    }
    
}
