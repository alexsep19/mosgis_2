package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisFileLogTable;

public class AccountIndividualServiceLog extends GisFileLogTable {

    public AccountIndividualServiceLog () {

        super ("tb_account_svc__log", "История редактирования индивидуальных услуг лицевых счетов", AccountIndividualService.class
            , EnTable.c.class
            , AttachTable.c.class
            , AccountIndividualService.c.class
        );
        
    }
    
}