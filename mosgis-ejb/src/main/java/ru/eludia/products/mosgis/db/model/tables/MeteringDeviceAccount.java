package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;

public class MeteringDeviceAccount extends Table {

    public MeteringDeviceAccount () {

        super ("tb_meter_acc", "Связь приборов учёта с лицевыми счетами");

        pkref ("uuid",         MeteringDevice.class,  "Прибор учёта");
        pkref ("uuid_account", Account.class,         "Лицевой счёт");

    }

}