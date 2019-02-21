package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisFileLogTable;

public class MeteringDeviceFileLog extends GisFileLogTable {

    public MeteringDeviceFileLog () {

        super ("tb_meter_files__log", "История редактирования [сведений о размере платы за] услуги управления", MeteringDeviceFile.class
            , EnTable.c.class
            , AttachTable.c.class
            , MeteringDeviceFile.c.class
        );
        
    }
    
}