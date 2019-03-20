package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class LegalActLog extends GisWsLogTable {

    public LegalActLog () {

        super ("tb_legal_acts__log", "История редактирования НПА", LegalAct.class
            , EnTable.c.class
            , LegalAct.c.class
        );
    }
}