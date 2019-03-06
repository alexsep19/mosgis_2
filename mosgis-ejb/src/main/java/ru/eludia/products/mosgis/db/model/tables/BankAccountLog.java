package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class BankAccountLog extends GisWsLogTable {

    public BankAccountLog () {

        super ("tb_bnk_accts__log", "История редактирования платёжных реквизитов (расчётных счетов)", BankAccount.class
            , EnTable.c.class
            , BankAccount.c.class
        );

    }
    
}