package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class SupplyResourceContractObjectLog extends GisWsLogTable {

    public SupplyResourceContractObjectLog () {

        super ("tb_sr_ctr_obj__log", "История редактирования объекта жилищного фонда договора ресурсоснабжения", SupplyResourceContractObject.class
            , EnTable.c.class
            , SupplyResourceContractObject.c.class

        );
    }
}