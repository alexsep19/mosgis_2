package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;

public class VocOrganizationNsi20 extends Table {

    public static final String TABLE_NAME = "vc_orgs_nsi_20";

    public VocOrganizationNsi20 () {

        super  (TABLE_NAME,  "Полномочия организаций");

        pkref  ("uuid",       VocOrganization.class, "Организация");
        pk     ("code",       Type.STRING, 20, "Код полномочия (НСИ 20)");
        col    ("is_deleted", Type.BOOLEAN,          Bool.FALSE,          "1, если запись удалена; иначе 0");

        trigger ("BEFORE INSERT OR UPDATE",

            "BEGIN "
                    
                + "IF :NEW.code IN ('19', '20', '21', '22') THEN "

                    + "MERGE INTO " 
                    + "  tb_charters o " 
                    + "USING " 
                    + " (SELECT orgrootentityguid uuid_org, stateregistrationdate date_ FROM vc_orgs WHERE orgrootentityguid = :NEW.uuid) n " 
                    + "ON " 
                    + " (o.uuid_org=n.uuid_org) "
                    + "WHEN MATCHED THEN "
                    + " UPDATE SET is_deleted=0, date_=n.date_ "
                    + "WHEN NOT MATCHED THEN "
                    + " INSERT (uuid_org, date_) VALUES (n.uuid_org, n.date_); "

                + "END IF; "

            + "END;"

        );

    }

}