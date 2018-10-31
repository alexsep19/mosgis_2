package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class ActualCaChObject extends View {

    public ActualCaChObject () {
        
        super  ("vw_ca_ch_objects", "Объекты договоров управления и уставов, определяющие права доступа в данный момент");
        
        pk     ("uuid",                    Type.UUID,             null, "Ключ");        
        ref    ("uuid_contract",           Contract.class,        null, "Ссылка на договор (NULL для объекта устава)");
        fk     ("fiashouseguid",           VocBuilding.class,     null, "Глобальный уникальный идентификатор дома по ФИАС");
        fk     ("uuid_org",                VocOrganization.class, null, "Исполнитель/Организация");
        fk     ("id_ctr_status_gis",       VocGisStatus.class,    null, "Статус объекта с точки зрения ГИС ЖКХ");
        
    }

    @Override
    public final String getSQL () {

        return ""
            + "SELECT uuid, fiashouseguid, uuid_org, id_ctr_status_gis, uuid_contract FROM " + getName (ActualContractObject.class)
            + " UNION "
            + "SELECT uuid, fiashouseguid, uuid_org, id_ctr_status_gis, NULL uuid_contract FROM " + getName (ActualCharterObject.class)
        ;

    }

}