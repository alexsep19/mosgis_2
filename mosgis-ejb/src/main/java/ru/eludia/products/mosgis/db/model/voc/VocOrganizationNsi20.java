package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class VocOrganizationNsi20 extends Table {

    public VocOrganizationNsi20 () {

        super  ("vc_orgs_nsi_20",  "Полномочия организаций");
        
        pkref  ("uuid", VocOrganization.class, "Организация");
        pk     ("code", Type.STRING, 20, "Код полномочия (НСИ 20)");
       
    }

}