package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class ActualCharterObject extends View {

    public ActualCharterObject () {        
        super  ("vw_charter_objects", "Объекты уставов, определяющие права доступа в данный момент");        
        cols   (ActualCaChObject.c.class);
        pk     (ActualCaChObject.c.UUID);
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " o.uuid"
            + " , NULL uuid_contract"
            + " , uuid_charter"
            + " , o.fiashouseguid"
            + " , o.id_ctr_status_gis"
            + " , c.uuid_org"
            + " , DECODE(c.id_ctr_state_gis, " + VocGisStatus.i.RUNNING + ", 1 - o.ismanagedbycontract, 0) is_own"
            + " , c.ddt_m_start"
            + " , c.ddt_m_start_nxt"
            + " , c.ddt_d_start ddt_m_end"
            + " , c.ddt_d_start_nxt ddt_m_end_nxt"
            + " FROM "       + getName (CharterObject.class) + " o"
            + " INNER JOIN " + getName (Charter.class) + " c ON o.uuid_charter = c.uuid"
            + " WHERE o.is_deleted = 0"
            + " AND o.id_ctr_status_gis <> " + VocGisStatus.i.ANNUL.getId ()
            + " AND o.startdate <= SYSDATE"
            + " AND (o.enddate IS NULL OR o.enddate >= TRUNC(SYSDATE))"
        ;

    }

}