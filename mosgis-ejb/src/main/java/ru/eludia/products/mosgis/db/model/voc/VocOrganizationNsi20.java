package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;

public class VocOrganizationNsi20 extends Table {

    public VocOrganizationNsi20 () {

        super  ("vc_orgs_nsi_20",  "Полномочия организаций");
        
        pkref  ("uuid",       VocOrganization.class, "Организация");
        pk     ("code",       Type.STRING, 20, "Код полномочия (НСИ 20)");
        col    ("is_deleted", Type.BOOLEAN,          Bool.FALSE,          "1, если запись удалена; иначе 0");
        
        trigger ("BEFORE INSERT OR UPDATE",
                
            "BEGIN "

                + "IF :NEW.code IN ('19', '21', '22') THEN "

                    + "MERGE INTO " 
                    + "  tb_charters o " 
                    + "USING " 
                    + " (SELECT :NEW.uuid uuid_org FROM DUAL) n " 
                    + "ON " 
                    + " (o.uuid_org=n.uuid_org) " 
                    + "WHEN MATCHED THEN " 
                    + " UPDATE SET is_deleted=0 " 
                    + "WHEN NOT MATCHED THEN " 
                    + " INSERT (uuid_org) VALUES (:NEW.uuid); "

                + "END IF; "

            + "END;"
                
        );

    }

}