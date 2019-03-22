package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class GeneralNeedsMunicipalResourceLog extends GisWsLogTable {

    public GeneralNeedsMunicipalResourceLog () {

        super (GeneralNeedsMunicipalResource.TABLE_NAME + "__log", "История редактирования НСИ 337", GeneralNeedsMunicipalResource.class
            , EnTable.c.class
            , GeneralNeedsMunicipalResource.c.class
        );

    }

}