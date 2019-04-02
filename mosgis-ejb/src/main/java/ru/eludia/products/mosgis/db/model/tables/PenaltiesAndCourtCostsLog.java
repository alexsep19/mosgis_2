package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class PenaltiesAndCourtCostsLog extends GisWsLogTable {

    public PenaltiesAndCourtCostsLog () {

        super (PenaltiesAndCourtCosts.TABLE_NAME + "__log", "История редактирования неустоек и судебных расходов", PenaltiesAndCourtCosts.class
            , EnTable.c.class
            , PenaltiesAndCourtCosts.c.class
        );

    }

}