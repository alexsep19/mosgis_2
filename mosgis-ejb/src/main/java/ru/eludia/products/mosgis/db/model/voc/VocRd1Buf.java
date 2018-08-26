package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.db.dialect.Oracle;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class VocRd1Buf extends Table {

    public VocRd1Buf () {

        super  ("vc_rd_1_buf",         "Буфер для импорта МКД/ЖД");

        setTemporality (Oracle.TemporalityType.GLOBAL, Oracle.TemporalityRowsAction.PRESERVE);

        pk     ("id",                    Type.NUMERIC, 12,                 "id объекта в WS ГИС РД");
        col    ("address",               Type.STRING,                      "Адрес");

    }

}