package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.incoming.InOpenDataLine;

public class VocRd1HouseRaw extends View {

    public VocRd1HouseRaw () {

        super  ("vc_rd_1_houses_raw",   "Объекты WS ГИС РД, представленные как МКД/ЖД, с возможными дублями");

        pk     ("id",                    Type.NUMERIC, 12,                 "id объекта в WS ГИС РД");        
        col    ("address",               Type.STRING,                      "Адрес");
        col    ("unom",                  Type.NUMERIC, 12, null,           "UNOM (код дома в московских ИС)");        
        col    ("is_condo",              Type.BOOLEAN, null,               "1 для МКД, 0 для ЖД");
        fk     ("fiashouseguid",         VocBuilding.class, null,          "Глобальный уникальный идентификатор дома по ФИАС");

    }

    @Override
    public String getSQL () {
        
        return 
            " SELECT" +
            "  o.id" +
            "  , o.unom" +
            "  , o.address" +
            "  , DECODE (o.id_vc_rd_1240, 42875644, 1, 0) AS is_condo" +
            "  , v.fiashouseguid" +
            " FROM " +
              getName (VocRd1.class) + " o" +
            "  LEFT JOIN " + getName (InOpenDataLine.class) + " v ON v.unom = o.unom" +
            " WHERE" +
            "  o.unom IS NOT NULL" +
            "  AND o.id_vc_rd_1540 = 58761330" +
            "  AND o.id_vc_rd_1240 IN (42875644, 22741963)";

    }

}