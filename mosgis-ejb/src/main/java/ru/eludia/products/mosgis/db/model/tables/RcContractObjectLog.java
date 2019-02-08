package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class RcContractObjectLog extends LogTable {

    public RcContractObjectLog () {

        super ("tb_rc_ctr_obj__log", "История редактирования объекта жилищного фонда договора РЦ", RcContractObject.class
            , EnTable.c.class
            , RcContractObject.c.class

        );
    }
}