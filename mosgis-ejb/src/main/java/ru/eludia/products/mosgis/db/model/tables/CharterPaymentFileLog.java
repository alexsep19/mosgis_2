package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisFileLogTable;

public class CharterPaymentFileLog extends GisFileLogTable {

    public CharterPaymentFileLog () {

        super ("tb_ch_pay_files__log", "История редактирования [сведений о размере платы за] услуги управления", CharterPaymentFile.class
            , EnTable.c.class
            , AttachTable.c.class
            , CharterPaymentFile.c.class
        );
        
    }
    
}