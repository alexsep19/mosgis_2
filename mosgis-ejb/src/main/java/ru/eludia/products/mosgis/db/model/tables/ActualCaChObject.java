package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class ActualCaChObject extends View {
    
    public enum c implements ColEnum {
        
        UUID                      (Type.UUID,     null,           "Ключ"),        
        UUID_CONTRACT             (Contract.class,                "Ссылка на договор (NULL для объекта устава)"),
        FIASHOUSEGUID             (House.class,                   "Глобальный уникальный идентификатор дома по ФИАС"),
        UUID_ORG                  (VocOrganization.class,         "Исполнитель/Организация"),
        ID_CTR_STATUS_GIS         (VocGisStatus.class,            "Статус объекта с точки зрения ГИС ЖКХ"),
        IS_OWN                    (BOOLEAN,                       "1, если объект даёт право редактирования паспорта; иначе 0")
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
                
    }    
    

    public ActualCaChObject () {        
        super  ("vw_ca_ch_objects", "Объекты договоров управления и уставов, определяющие права доступа в данный момент");
        cols   (c.class);
        pk     (c.UUID);        
    }

    @Override
    public final String getSQL () {

        return ""
            + "SELECT uuid, fiashouseguid, uuid_org, id_ctr_status_gis, uuid_contract, is_own FROM " + getName (ActualContractObject.class)
            + " UNION "
            + "SELECT uuid, fiashouseguid, uuid_org, id_ctr_status_gis, uuid_contract, is_own FROM " + getName (ActualCharterObject.class)
        ;

    }

}