package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class AcknowledgmentLog extends GisWsLogTable {

    public AcknowledgmentLog () {

        super (Acknowledgment.TABLE_NAME + "__log", "История редактирования записей квитирования", Acknowledgment.class
            , EnTable.c.class
            , Acknowledgment.c.class
        );

    }

}