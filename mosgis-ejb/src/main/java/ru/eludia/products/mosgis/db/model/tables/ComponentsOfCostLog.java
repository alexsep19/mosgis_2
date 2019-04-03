package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class ComponentsOfCostLog extends GisWsLogTable {

    public ComponentsOfCostLog () {

        super (ComponentsOfCost.TABLE_NAME + "__log", "История редактирования составляющих стоимости электрической энергии", ComponentsOfCost.class
            , EnTable.c.class
            , ComponentsOfCost.c.class
        );

    }

}