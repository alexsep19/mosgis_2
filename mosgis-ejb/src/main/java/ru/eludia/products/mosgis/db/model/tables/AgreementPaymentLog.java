package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class AgreementPaymentLog extends GisWsLogTable {
    
    public AgreementPaymentLog () {

        super ("tb_pp_ctr_ap__log", "История редактирования платы по договорам на пользование общим имуществом", AgreementPayment.class
            , EnTable.c.class
            , AgreementPayment.c.class
        );
        
    }
    
}