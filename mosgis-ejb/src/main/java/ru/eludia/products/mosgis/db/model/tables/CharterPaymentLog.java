package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class CharterPaymentLog extends GisWsLogTable {

    public CharterPaymentLog () {

        super ("tb_ch_payments__log", "История редактирования [сведений о размере платы за] услуги управления", CharterPayment.class
            , EnTable.c.class
            , CharterPayment.c.class
        );
        
    }
                
}