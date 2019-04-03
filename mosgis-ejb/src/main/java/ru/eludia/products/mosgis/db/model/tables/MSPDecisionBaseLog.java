package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class MSPDecisionBaseLog extends GisWsLogTable {

    public MSPDecisionBaseLog () {

        super (MSPDecisionBase.TABLE_NAME + "__log", "История редактирования НСИ 302", MSPDecisionBase.class
            , EnTable.c.class
            , MSPDecisionBase.c.class
        );
    }
}