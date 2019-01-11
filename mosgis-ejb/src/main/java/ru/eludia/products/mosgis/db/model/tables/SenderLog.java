package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class SenderLog extends LogTable {

    public SenderLog () {

        super ("tb_senders__log", "История редактирования поставщиков данных", Sender.class
            , EnTable.c.class
            , Sender.c.class
        );
        
    }

}