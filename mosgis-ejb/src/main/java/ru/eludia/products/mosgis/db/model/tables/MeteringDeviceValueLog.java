package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class MeteringDeviceValueLog extends GisWsLogTable {

    public MeteringDeviceValueLog () {

        super ("tb_meter_values__log", "История редактирования показаний приборов учёта", MeteringDevice.class
            , EnTable.c.class
            , MeteringDeviceValue.c.class
        );

    }

}