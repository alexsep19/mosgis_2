package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.db.dialect.Oracle;
import ru.eludia.base.model.Table;

public class VocUnomBuf extends Table {

    public VocUnomBuf () {

        super ("vc_unom_buf", "Буфер для загрузки мапинга UNOM");

        cols (VocUnom.c.class);

        pk (VocUnom.c.UNOM);
        
        setTemporality (Oracle.TemporalityType.GLOBAL, Oracle.TemporalityRowsAction.PRESERVE);
        
    }

}