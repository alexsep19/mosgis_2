package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class PublicPropertyContractLog extends GisWsLogTable {

    public PublicPropertyContractLog () {

        super ("tb_pp_ctr__log", "История редактирования [сведений о размере платы за] услуги управления", PublicPropertyContract.class
            , EnTable.c.class
            , PublicPropertyContract.c.class
        );
        
    }
                
}