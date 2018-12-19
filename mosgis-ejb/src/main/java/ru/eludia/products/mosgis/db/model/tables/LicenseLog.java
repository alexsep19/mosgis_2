package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.LogTable;

public class LicenseLog extends LogTable {
    
//    private static final ObjectFactory of = new ObjectFactory ();

    public LicenseLog () {

        super  ("tb_licenses__log","Лицензии: история", License.class,
                License.c.class
        );

        fk    ("uuid_out_soap", OutSoap.class, null, "Последний запрос на импорт в ГИС ЖКХ");
    }
    
//    public static final RegOrgType regOrgType (UUID uuid) {
//        final RegOrgType o = of.createRegOrgType ();
//        o.setOrgRootEntityGUID (uuid.toString ());
//        return o;
//    }

} 
