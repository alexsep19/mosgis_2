package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class SettlementDocPaymentLog extends LogTable {

    public SettlementDocPaymentLog () {

        super ("tb_st_doc_ps__log", "История редактирования расчетов договора ресурсоснабжения", SettlementDocPayment.class
            , EnTable.c.class
            , SettlementDocPayment.c.class

        );
    }
}