package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class ContractPaymentLog extends GisWsLogTable {

    public ContractPaymentLog () {

        super ("tb_ctr_payments__log", "История редактирования [сведений о размере платы за] услуги управления", ContractPayment.class
            , EnTable.c.class
            , ContractPayment.c.class
        );
        
    }
                
}