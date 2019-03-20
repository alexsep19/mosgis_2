package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class PaymentDocumentLog extends GisWsLogTable {

    public PaymentDocumentLog () {

        super (PaymentDocument.TABLE_NAME + "__log", "История редактирования платёжных документов", PaymentDocument.class
            , EnTable.c.class
            , PaymentDocument.c.class
        );

    }

}