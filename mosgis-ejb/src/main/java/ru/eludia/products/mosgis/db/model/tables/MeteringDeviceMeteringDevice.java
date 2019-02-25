package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;

public class MeteringDeviceMeteringDevice extends Table {

    public MeteringDeviceMeteringDevice () {

        super ("tb_meter_meters", "Связь приборов учёта с приборами учёта");

        pkref ("uuid",       MeteringDevice.class,  "Прибор учёта");
        pkref ("uuid_meter", MeteringDevice.class,  "Связанный прибор учёта");

    }

}