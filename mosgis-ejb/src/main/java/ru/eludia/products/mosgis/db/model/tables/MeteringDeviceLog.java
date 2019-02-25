package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.gosuslugi.dom.schema.integration.house_management.ImportMeteringDeviceDataRequest;

public class MeteringDeviceLog extends GisWsLogTable {

    public MeteringDeviceLog () {

        super ("tb_meters__log", "История редактирования приборов учёта", MeteringDevice.class
            , EnTable.c.class
            , MeteringDevice.c.class
        );

    }

    public static ImportMeteringDeviceDataRequest toImportMeteringDeviceData (Map<String, Object> r) {
        final ImportMeteringDeviceDataRequest result = DB.to.javaBean (ImportMeteringDeviceDataRequest.class, r);
//        result.set
        return result;
    }
    
}