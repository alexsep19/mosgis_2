package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class MainMunicipalServiceLog extends GisWsLogTable {

    public MainMunicipalServiceLog () {

        super (MainMunicipalService.TABLE_NAME + "__log", "Коммунальные услуги: история изменения", MainMunicipalService.class
            , EnTable.c.class
            , MainMunicipalService.c.class
        );

        col   ("elementguid_new",           Type.UUID,                          null,   "Идентификатор новой версии существующего (в ГИС) элемента справочника");         
        
        trigger ("AFTER INSERT", "BEGIN "
            + " RETURN;"
        + "END;");

    }

}