package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class ServicePaymentLog extends LogTable {

    public ServicePaymentLog () {

        super ("tb_svc_payments__log", "История редактирования: информация о размере платы (цене, тарифе) за содержание и текущий ремонт общего имущества в многоквартирном доме/Информация о размере платы за содержание жилого помещения, установленном по результатам открытого конкурса по отбору управляющей организации для управления многоквартирным домом", ContractPayment.class
            , EnTable.c.class
            , ServicePayment.c.class
        );
        
    }
                
}