package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class IntervalObjectLog extends LogTable {

    public IntervalObjectLog () {

        super ("tb_interval_obj__log", "История редактирования ОЖФ информации о перерывах РСО", IntervalObject.class
            , EnTable.c.class
            , IntervalObject.c.class
        );
    }
}