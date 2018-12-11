package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.products.mosgis.db.model.LogTable;
import java.util.UUID;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.RegOrgType;

public class VocOrganizationLog extends LogTable {
    
    private static final ObjectFactory of = new ObjectFactory ();

    public VocOrganizationLog () {

        super  ("vc_orgs__log","Юридические лица и частные предприниматели: история", VocOrganization.class,
                VocOrganization.c.class
        );

        fk    ("uuid_out_soap", OutSoap.class, null, "Последний запрос на импорт в ГИС ЖКХ");
    }
    
    public static final RegOrgType regOrgType (UUID uuid) {
        final RegOrgType o = of.createRegOrgType ();
        o.setOrgRootEntityGUID (uuid.toString ());
        return o;
    }

}