package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class SettlementDocLog extends GisWsLogTable {

    public SettlementDocLog () {

        super ("tb_st_docs__log", "История редактирования документов расчетов договора ресурсоснабжения", SettlementDoc.class
            , EnTable.c.class
            , SettlementDoc.c.class

        );
    }
}