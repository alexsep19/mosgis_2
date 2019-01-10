package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class SupplyResourceContractLog extends GisWsLogTable {

    public SupplyResourceContractLog () {

        super ("tb_sr_ctr__log", "История редактирования договоров ресурсоснабжения", SupplyResourceContract.class
            , EnTable.c.class
            , SupplyResourceContract.c.class
        );

    }
}