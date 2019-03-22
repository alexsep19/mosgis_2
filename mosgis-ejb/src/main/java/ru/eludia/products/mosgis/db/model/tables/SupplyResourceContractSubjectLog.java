package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class SupplyResourceContractSubjectLog extends GisWsLogTable {

    public SupplyResourceContractSubjectLog () {

        super ("tb_sr_ctr_subj__log", "История редактирования предмета договора ресурсоснабжения", SupplyResourceContractSubject.class
            , EnTable.c.class
            , SupplyResourceContractSubject.c.class
        );
    }
    
}