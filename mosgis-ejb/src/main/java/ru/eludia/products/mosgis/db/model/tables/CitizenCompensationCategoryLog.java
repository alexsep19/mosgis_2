package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class CitizenCompensationCategoryLog extends GisWsLogTable {

    public CitizenCompensationCategoryLog () {

        super (CitizenCompensationCategory.TABLE_NAME + "__log", "История редактирования Перечень отдельных категорий граждан, имеющих право на получение компенсации расходов", CitizenCompensationCategory.class
            , EnTable.c.class
            , CitizenCompensationCategory.c.class
        );
    }
}