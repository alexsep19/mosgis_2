package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class ActualSupplyResourceContractObject extends View {
    
    public enum c implements ColEnum {
        
        UUID                      (Type.UUID,     null,           "Ключ"),
        UUID_SR_CTR               (SupplyResourceContract.class,  "Ссылка на договор"),
        FIASHOUSEGUID             (VocBuilding.class,             "Глобальный уникальный идентификатор дома по ФИАС"),
        UUID_ORG                  (VocOrganization.class,         "Исполнитель/Организация"),
        ID_CTR_STATUS             (VocGisStatus.class,            "Статус объекта с точки зрения mosgis")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
                
    }  
    
    public ActualSupplyResourceContractObject () {
        
        super ("vw_ca_sr_objects", "Объекты договоров ресурсоснабжения, определяющие права доступа в данный момент");
        cols  (c.class);
        pk    (c.UUID);
        
    }
    
    @Override
    public String getSQL() {
        
        return 
            "SELECT "
                + "o.uuid, "
                + "o.uuid_sr_ctr, "
                + "o.fiashouseguid, "
                + "o.id_ctr_status, "
                + "c.uuid_org "
            + "FROM "
                + getName (SupplyResourceContractObject.class) + " o "
            + "INNER JOIN "
                + getName (SupplyResourceContract.class) + " c "
                    + "ON o.uuid_sr_ctr = c.uuid "
            + "WHERE o.is_deleted = 0 "
            + "AND o.id_ctr_status <> " + VocGisStatus.i.ANNUL.getId () + " "
            + "AND c.effectivedate <= SYSDATE "
            + "AND (c.completiondate IS NULL OR c.completiondate >= TRUNC(SYSDATE)) "
            + "AND (c.terminate IS NULL OR c.terminate >= TRUNC (SYSDATE)) "
        ;
        
    }
    
}
