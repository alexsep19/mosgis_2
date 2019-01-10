package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisFileLogTable;

public class SupplyResourceContractFileLog extends GisFileLogTable {

    public SupplyResourceContractFileLog () {

        super ("tb_sr_ctr_files__log", "История редактирования файлов, приложенных к к договорам ресурсоснабжения"
            , SupplyResourceContractFile.class
            , EnTable.c.class
            , AttachTable.c.class
        );
    }
}
