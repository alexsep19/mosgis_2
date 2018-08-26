package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;

public class VocRd1 extends Table {

    public VocRd1 () {

        super  ("vc_rd_1",         "Объекты МКД/ЖД с точки здерния WS ГИС РД");

        pk     ("id",                    Type.NUMERIC, 12,                 "id объекта в WS ГИС РД");        
        col    ("address",               Type.STRING,                      "Адрес");
        col    ("address_uc",            Type.STRING,  new Virt ("UPPER(\"ADDRESS\")"),  "АДРЕС В ВЕРХНЕМ РЕГИСТРЕ");

        col    ("unom",                  Type.NUMERIC, 12, null,           "UNOM (код дома в московских ИС)");        
        col    ("id_vc_rd_1240",         Type.INTEGER, null,               "вид жилого фонда");
        col    ("id_vc_rd_1540",         Type.INTEGER, null,               "статус");

        col    ("ts_get_object",         Type.DATE, null,                  "Время последнего вызова GetObjectByObjectID");

        key    ("unom", "unom");
        key    ("address", "address");

    }

}