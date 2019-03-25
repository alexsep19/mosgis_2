package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class ChargeInfoLog extends GisWsLogTable {

    public ChargeInfoLog () {

        super (ChargeInfo.TABLE_NAME + "__log", "История редактирования строк начислений в платёжных документах", ChargeInfo.class
            , EnTable.c.class
            , ChargeInfo.c.class
        );

    }

}