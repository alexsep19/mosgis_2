package ru.eludia.products.mosgis.db.model.gis;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;

public class EntranceGis extends Table {

    public EntranceGis () {
        
        super ("gis_entrances", "Подъезды из ГИС ЖКХ");
        
        pk     ("uuid",                    Type.UUID,                      "Идентификатор подъезда");
        
        ref    ("uuid_house_gis",          HouseGis.class,                 "Дом");
        ref    ("fias_child_house_guid",   VocBuilding.class,   null,      "ГУИД дочернего дома по ФИАС, к которому относится подъезд для группирующих домов");
        
        col    ("entrance_num",            Type.STRING,  255,              "Номер подъезда");
        col    ("storeys_count",           Type.NUMERIC,   3,   null,       "Этажность");
        col    ("creation_year",           Type.NUMERIC,   4,   null,       "Год постройки");
        
        col    ("termination_date",        Type.DATE,           null,      "Дата аннулирования объекта в ГИС ЖКХ");
        col    ("code_vc_nsi_330",         Type.STRING,   20,   null,      "Причина аннулирования объекта жилищного фонда (НСИ 330)");
        col    ("annulment_info",          Type.STRING,         null,      "Причина аннулирования. Дополнительная информация");
        
        col    ("gis_modification_date",   Type.DATETIME,       null,      "Дата модификации данных подъезда в ГИС ЖКХ");
        
        col    ("information_confirmed",   Type.BOOLEAN,        Bool.TRUE, "Информация подтверждена поставщиком");
        
    }
    
}