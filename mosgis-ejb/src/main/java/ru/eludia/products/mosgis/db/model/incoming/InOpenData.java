package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;

public class InOpenData extends Table {

    public InOpenData () {
        
        super ("in_open_data",     "Импорты портала открытых жданных Правительства Москвы");
        
        pk    ("uuid",        Type.UUID, NEW_UUID, "Ключ");
        
        col   ("dt",          Type.DATE,        null, "Дата выгрузки");
        col   ("no",          Type.NUMERIC, 10, null, "Номер выгрузки");
        
        col   ("dt_from",     Type.DATE, null,  "Дата начала процесса");
        col   ("dt_to_fact",  Type.DATE, null,  "Дата окончания процеса");

        col   ("sz",  Type.NUMERIC, 12, 0, Num.ZERO, "Суммарный Размер XML-файлов");
        col   ("rd",  Type.NUMERIC, 12, 0, Num.ZERO, "Сколько прочитано XML-файлов, в байтах");
        
        key ("dt_from", "dt_from");
                        
    }
    
}