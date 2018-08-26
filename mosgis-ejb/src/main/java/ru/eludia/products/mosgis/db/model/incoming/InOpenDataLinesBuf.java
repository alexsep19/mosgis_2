package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class InOpenDataLinesBuf extends Table {

    public InOpenDataLinesBuf () {

        super ("in_open_data_lines_buf",    "Буфер для строк импорта выгрузки портала открытых данных Правительства Москвы");

        pk    ("unom",                  Type.NUMERIC, 12,                 "UNOM (код дома в московских ИС)");
        col   ("fiashouseguid",         Type.UUID,   null,                "Глобальный уникальный идентификатор дома по ФИАС");
        col   ("address",               Type.STRING, null,                "Адрес");
        col   ("kad_n",                 Type.STRING, null,                "Кадастровый номер");
        col   ("fn",                    Type.NUMERIC, 1,                  "Номер файла");
        col   ("line",                  Type.NUMERIC, 6,                  "Номер строки");
        fk    ("uuid_in_open_data",     InOpenData.class,                 "Загрузка");

        key   ("fiashouseguid", "fiashouseguid");

    }

}