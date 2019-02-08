package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class RcContractLog extends GisWsLogTable {

    public RcContractLog () {

        super ("tb_rc_ctr__log", "История редактирования договоров РЦ", RcContract.class
            , EnTable.c.class
            , RcContract.c.class
        );

    }
}