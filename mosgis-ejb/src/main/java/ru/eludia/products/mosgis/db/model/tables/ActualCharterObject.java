package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class ActualCharterObject extends View {

    public ActualCharterObject () {
        
        super  ("vw_charter_objects", "Объекты уставов, определяющие права доступа в данный момент");
        
        pk     ("uuid",                    Type.UUID,             null, "Ключ");        
        ref    ("uuid_contract",           Contract.class,        null, "Ссылка на договор (всегда NULL)");
        fk     ("fiashouseguid",           VocBuilding.class,     null, "Глобальный уникальный идентификатор дома по ФИАС");
        fk     ("uuid_org",                VocOrganization.class, null, "Организация");
        fk     ("id_ctr_status_gis",       VocGisStatus.class,    null, "Статус объекта с точки зрения ГИС ЖКХ");
        
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " o.uuid"
            + " , o.fiashouseguid"
            + " , o.id_ctr_status_gis"
            + " , c.uuid_org"
            + " FROM "       + getName (CharterObject.class) + " o"
            + " INNER JOIN " + getName (Charter.class) + " c ON o.uuid_charter = c.uuid"
            + " WHERE o.is_deleted = 0"
            + " AND o.ismanagedbycontract = 0"
            + " AND o.id_ctr_status_gis <> " + VocGisStatus.i.ANNUL.getId ()
            + " AND o.startdate <= SYSDATE"
            + " AND (o.enddate IS NULL OR o.enddate >= TRUNC(SYSDATE))"
        ;

    }

}