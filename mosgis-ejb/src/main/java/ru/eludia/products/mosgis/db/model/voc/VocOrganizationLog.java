package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import java.util.UUID;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.RegOrgType;

public class VocOrganizationLog extends GisWsLogTable {
    
    private static final ObjectFactory of = new ObjectFactory ();

    public VocOrganizationLog () {

        super  ("vc_orgs__log","Юридические лица и частные предприниматели: история", VocOrganization.class,
                VocOrganization.c.class
        );

    }
    
    public static final RegOrgType regOrgType (UUID uuid) {
        final RegOrgType o = of.createRegOrgType ();
        o.setOrgRootEntityGUID (uuid.toString ());
        return o;
    }

}