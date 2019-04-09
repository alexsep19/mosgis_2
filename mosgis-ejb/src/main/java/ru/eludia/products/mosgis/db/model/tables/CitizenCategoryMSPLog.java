package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class CitizenCategoryMSPLog extends GisWsLogTable {

    public CitizenCategoryMSPLog () {

        super (CitizenCategoryMSP.TABLE_NAME + "__log", "История редактирования Перечень отдельных категорий граждан, имеющих право на получение компенсации расходов", CitizenCategoryMSP.class
            , EnTable.c.class
            , CitizenCategoryMSP.c.class
        );
    }
}