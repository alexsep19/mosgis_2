package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Bool.TRUE;
import ru.eludia.base.model.def.Virt;

public class InOpenDataLine extends Table {

    public InOpenDataLine () {

        super ("in_open_data_lines",     "Строки импорта выгрузки портала открытых данных Правительства Москвы");

        pk    ("unom",                  Type.NUMERIC, 12,                 "UNOM (код дома в московских ИС)");
        col   ("fiashouseguid",         Type.UUID,   null,                "Глобальный уникальный идентификатор дома по ФИАС");
        col   ("address",               Type.STRING, null,                "Адрес");
        col   ("kad_n",                 Type.STRING, null,                "Кадастровый номер");
        col   ("fn",                    Type.NUMERIC, 1,                  "Номер файла");
        col   ("line",                  Type.NUMERIC, 6,                  "Номер строки");
        col   ("is_actual",             Type.BOOLEAN, TRUE,               "Признак актуальности");
        fk    ("uuid_in_open_data",     InOpenData.class,                 "Последняя загрузка, в которой была данная запись");

        key   ("fiashouseguid", "fiashouseguid");
        key   ("address", "address");
        key   ("address_uc", "address_uc");
        
        col   ("address_uc",            Type.STRING,  new Virt ("UPPER(\"ADDRESS\")"),  "АДРЕС В ВЕРХНЕМ РЕГИСТРЕ");

    }

}