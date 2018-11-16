package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class ActualContractObject extends View {

    public ActualContractObject () {        
        super  ("vw_contract_objects", "Объекты договоров управления, определяющие права доступа в данный момент");
        cols   (ActualCaChObject.c.class);
        pk     (ActualCaChObject.c.UUID);        
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " o.uuid"
            + " , o.uuid_contract"
            + " , o.fiashouseguid"
            + " , o.id_ctr_status_gis"
            + " , c.uuid_org"
            + " , 1 is_own"
            + " FROM "       + getName (ContractObject.class) + " o"
            + " INNER JOIN " + getName (Contract.class) + " c ON o.uuid_contract = c.uuid"
            + " WHERE o.is_deleted = 0"
            + " AND o.id_ctr_status_gis <> " + VocGisStatus.i.ANNUL.getId ()
            + " AND o.startdate <= SYSDATE"
            + " AND (o.enddate IS NULL OR o.enddate >= TRUNC(SYSDATE))"
        ;

    }

}