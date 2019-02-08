package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class AccountItemLog extends LogTable {

    public AccountItemLog () {

        super ("tb_account_items__log", "История редактирования лицевых счетов", AccountItem.class
            , EnTable.c.class
            , AccountItem.c.class
        );
        
    }
    
}