package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class MeteringDeviceLog extends GisWsLogTable {

    public MeteringDeviceLog () {

        super ("tb_meters__log", "История редактирования приборов учёта", MeteringDevice.class
            , EnTable.c.class
            , MeteringDevice.c.class
        );

    }

}