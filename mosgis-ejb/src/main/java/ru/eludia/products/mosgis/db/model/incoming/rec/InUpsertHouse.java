package ru.eludia.products.mosgis.db.model.incoming.rec;

import ru.eludia.products.mosgis.db.model.incoming.json.InImportHouses;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;

public class InUpsertHouse extends Table {

    public InUpsertHouse () {
        
        super  ("in_tb_houses",                            "Входящие запросы на регистрацию МКД/ЖД");
        
        pk     ("transportguid",         Type.UUID,                        "Ключ");

        fk     ("fiashouseguid",         VocBuilding.class,                "Глобальный уникальный идентификатор дома по ФИАС ");
        col    ("totalsquare",           Type.NUMERIC, 13,                 "Общая площадь");
        col    ("usedyear",              Type.NUMERIC,  4, null,           "Год ввода в эксплуатацию");
        col    ("culturalheritage",      Type.BOOLEAN,     Bool.FALSE,     "Наличие у дома статуса объекта культурного наследия");
        col    ("floorcount",            Type.NUMERIC,  3, null,           "Количество этажей");
        col    ("minfloorcount",         Type.NUMERIC,  3, null,           "Количество этажей, наименьшее");
        col    ("undergroundfloorcount", Type.NUMERIC,  3, null,           "Количество подземных этажей");

        col    ("is_condo",              Type.BOOLEAN,                     "1 для МКД, 0 для ЖД");
        
        col    ("ts",             Type.TIMESTAMP,    NOW,           "Дата/время записи в БД");
        col    ("ts_done",        Type.TIMESTAMP,    null,          "Дата/время обработки");
        col    ("error",          Type.STRING,       null,          "Текст ошибки");

        ref    ("uuid_in_import", InImportHouses.class,              "Ссылка на охватывающий пакетный запрос");
        
    }
    
}