package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class ActualSomeContractObject extends View {
    
    public enum c implements ColEnum {
        
        UUID                      (Type.UUID,     null,           "Ключ"),        
        FIASHOUSEGUID             (House.class,                   "Глобальный уникальный идентификатор дома по ФИАС"),
        UUID_ORG                  (VocOrganization.class,         "Организация"),
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
                
    }    
    

    public ActualSomeContractObject () {        
        super  ("vw_some_ctr_objects", "Объекты каких-либо договоров, определяющие права доступа к паспортам домов в данный момент");
        cols   (c.class);
        pk     (c.UUID);        
    }

    @Override
    public final String getSQL () {

        return ""
            + "SELECT uuid, fiashouseguid, uuid_org FROM " + getName (ActualCaChObject.class)
            + " UNION "
            + "SELECT uuid, fiashouseguid, uuid_org FROM " + getName (ActualSupplyResourceContractObject.class)
        ;

    }

}