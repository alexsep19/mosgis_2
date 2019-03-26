package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class PremiseUsageTarifLog extends GisWsLogTable {

    public PremiseUsageTarifLog () {

        super ("tb_pu_tfs__log", "История редактирования тарифов Размер платы за пользование жилым помещением", PremiseUsageTarif.class
            , EnTable.c.class
            , PremiseUsageTarif.c.class
        );
    }
}