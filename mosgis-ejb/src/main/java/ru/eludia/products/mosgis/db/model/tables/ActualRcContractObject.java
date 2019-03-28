package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class ActualRcContractObject extends View {
    
    public static final String TABLE_NAME = "vw_rc_address_map";

    public enum c implements ColEnum {

        UUID                 (Type.UUID,       "Ключ"),        
	UUID_ORG             (VocOrganization.class, "Расчетный центр"),
	UUID_ORG_CUSTOMER    (VocOrganization.class, "Организация-заказчик"),
	DT_FROM              (Type.DATE, null, "Дата начала действия договора"),
	DT_TO                (Type.DATE, null, "Дата окончания действия договора"),        
	FIASHOUSEGUID        (VocBuilding.class, "Глобальный уникальный идентификатор дома по ФИАС"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }
    

    public ActualRcContractObject () {
        super  (TABLE_NAME, "Список действующих полномочий РЦ");
        cols   (c.class);
        pk     (c.UUID);
    }

    @Override
    public final String getSQL () {
        return 
            "SELECT uuid, uuid_org, uuid_org_customer, dt_from, dt_to, NULL fiashouseguid FROM " + RcContract.TABLE_NAME + " WHERE is_deleted = 0 AND id_ctr_status = 40 AND is_all_house = 1 AND id_service_type = 1"
            + " UNION " +
            "SELECT o.uuid, c.uuid_org, c.uuid_org_customer, o.dt_from, o.dt_to, o.fiashouseguid "
                + " FROM " + RcContract.TABLE_NAME + " c"
                + " INNER JOIN  " + RcContractObject.TABLE_NAME + " o ON o.UUID_RC_CTR=c.uuid AND o.is_deleted = 0"
                + " WHERE c.is_deleted = 0 AND c.id_ctr_status = 40 AND c.is_all_house = 0 AND c.id_service_type = 1"
        ;
    }

}