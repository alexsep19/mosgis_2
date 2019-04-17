package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class CitizenCompensationOverviewLog extends GisWsLogTable {

    public CitizenCompensationOverviewLog () {

        super (CitizenCompensation.TABLE_NAME + "__log", "История редактирования гражданин, получающих компенсации расходов", CitizenCompensation.class
            , EnTable.c.class
            , CitizenCompensation.c.class
        );
    }
}