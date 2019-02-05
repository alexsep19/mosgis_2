package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class AccountLog extends GisWsLogTable {

    public AccountLog () {

        super ("tb_accounts__log", "История редактирования лицевых счетов", Account.class
            , EnTable.c.class
            , Account.c.class
        );
        
    }
    
}