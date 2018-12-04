package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class VocOrganizationTerritory extends EnTable {
    
    public enum c implements EnColEnum {
        
        UUID_ORG    (VocOrganization.class, "Ссылка на юридическое лицо"),
        OKTMO       (VocOktmo.class,        "Ссылка на запись в ОКТМО")
        
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
    
    public VocOrganizationTerritory () {
        
        super ("vc_org_territories", "Территории юридического лица");
        
        cols (c.class);
        
        key ("uuid_org", c.UUID_ORG);
    }
    
}
