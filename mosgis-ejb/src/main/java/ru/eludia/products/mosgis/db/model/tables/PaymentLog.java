package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class PaymentLog extends GisWsLogTable {

    public PaymentLog () {

        super (Payment.TABLE_NAME + "__log", "История редактирования платежей", Payment.class
            , EnTable.c.class
            , Payment.c.class
        );

    }

}