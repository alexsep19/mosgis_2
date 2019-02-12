package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class IntervalLog extends GisWsLogTable {

    public IntervalLog () {

        super ("tb_intervals__log", "История редактирования информации о перерывах РСО", Interval.class
            , EnTable.c.class
            , Interval.c.class
        );
    }
}