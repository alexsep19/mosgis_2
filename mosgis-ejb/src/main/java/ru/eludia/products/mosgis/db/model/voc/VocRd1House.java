package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;

public class VocRd1House extends View {

    public VocRd1House () {

        super  ("vc_rd_1_houses",   "Объекты WS ГИС РД, представленные как МКД/ЖД");

        pk     ("id",                    Type.NUMERIC, 12,                 "id объекта в WS ГИС РД");        
        col    ("address",               Type.STRING,                      "Адрес");
        col    ("unom",                  Type.NUMERIC, 12, null,           "UNOM (код дома в московских ИС)");        
        col    ("is_condo",              Type.BOOLEAN, null,               "1 для МКД, 0 для ЖД");
        fk     ("fiashouseguid",         VocBuilding.class, null,          "Глобальный уникальный идентификатор дома по ФИАС");

    }

    @Override
    public String getSQL () {
        
        final String src = getName (VocRd1HouseRaw.class);  
        
        return 
            "SELECT id, unom, address, is_condo, fiashouseguid FROM " + src + " WHERE unom NOT IN (" +
                "  SELECT unom FROM " + src + " GROUP BY unom HAVING COUNT (id) > 1" +
            ")";
        
    }

}