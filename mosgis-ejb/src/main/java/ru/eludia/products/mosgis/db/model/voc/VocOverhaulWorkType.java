package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;

public class VocOverhaulWorkType extends Table {
    
    public enum c implements EnColEnum {
        
        GUID        (Type.UUID, "Глобально-уникальный идентификатор элемента справочника"),
        CODE        (Type.STRING, 20, "Код элемента справочника, уникальный в пределах справочника"),
        ISACTUAL    (Type.BOOLEAN, "Признак актуальности элемента справочника")
        
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
    
    public VocOverhaulWorkType () {
        
        super ("vc_oh_wk_types", "Справочник типов работ капитального ремонта");
        
        cols  (c.class);
        
        pk    (c.GUID);
        
    }
    
}
