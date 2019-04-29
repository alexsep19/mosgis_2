package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class VocBuildingLog extends GisWsLogTable {

    public VocBuildingLog () {

        super (VocBuilding.TABLE_NAME + "__log", "Запросы на импорт из ГИС ЖКХ паспортов домов по известым GUID ФИАС", VocBuilding.class
            , EnTable.c.class
            , VocBuilding.c.class
        );

    }
    
}