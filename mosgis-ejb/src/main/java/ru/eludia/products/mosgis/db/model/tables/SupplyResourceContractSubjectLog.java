package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class SupplyResourceContractSubjectLog extends LogTable {

    public SupplyResourceContractSubjectLog () {

        super ("tb_sr_ctr_subj__log", "История редактирования предмата договора ресурсоснабжения", SupplyResourceContractSubject.class
            , EnTable.c.class
            , SupplyResourceContractSubject.c.class

        );
    }
}