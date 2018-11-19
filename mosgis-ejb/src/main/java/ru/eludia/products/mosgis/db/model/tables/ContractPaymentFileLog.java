package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class ContractPaymentFileLog extends LogTable {

    public ContractPaymentFileLog () {

        super ("tb_ctr_pay_files__log", "История редактирования [сведений о размере платы за] услуги управления", ContractPaymentFile.class
            , EnTable.c.class
            , AttachTable.c.class
            , ContractPaymentFile.c.class
        );
        
    }
    
}