package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class VocBuildingLog extends GisWsLogTable {

    public static final String TABLE_NAME = VocBuilding.TABLE_NAME + "__log";

    public VocBuildingLog () {

        super (TABLE_NAME, "Запросы на импорт из ГИС ЖКХ паспортов домов по известым GUID ФИАС", VocBuilding.class
            , EnTable.c.class
            , VocBuilding.c.class
        );

    }
    
}