package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.db.dialect.Oracle;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;

public class HouseBuf extends Table {

    public HouseBuf () {
        
        super  ("tb_houses_buf",         "Буфер для импорта МКД/ЖД");
        
        setTemporality (Oracle.TemporalityType.GLOBAL, Oracle.TemporalityRowsAction.PRESERVE);
        
        pk     ("unom",                  Type.NUMERIC, 12,                 "UNOM (код дома в московских ИС)");        
        fk     ("fiashouseguid",         VocBuilding.class, null,          "Глобальный уникальный идентификатор дома по ФИАС");
        col    ("address",               Type.STRING,                      "Адрес");
        col    ("is_condo",              Type.BOOLEAN, null,               "1 для МКД, 0 для ЖД");        
        col    ("id_vc_rd_1240",         Type.INTEGER, null,               "вид жилого фонда");


        trigger ("BEFORE INSERT", "BEGIN "
                + " IF :NEW.fiashouseguid IS NULL THEN "
                + "  BEGIN"
                + "   SELECT fiashouseguid INTO :NEW.fiashouseguid FROM in_open_data_lines WHERE unom = :NEW.unom; "
                + "   EXCEPTION WHEN NO_DATA_FOUND THEN :NEW.fiashouseguid := NULL;"
                + "  END;"
                + " END IF;"
                + "END;");

    }

}