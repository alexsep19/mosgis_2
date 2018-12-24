package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class LicenseLog extends GisWsLogTable {

    public LicenseLog () {

        super  ("tb_licenses__log","Лицензии: история", License.class,
                License.c.class
        );

    }

} 
