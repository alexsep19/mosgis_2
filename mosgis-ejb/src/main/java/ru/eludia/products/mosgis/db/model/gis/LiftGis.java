package ru.eludia.products.mosgis.db.model.gis;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;

public class LiftGis extends Table {
    
    public LiftGis () {
        
        super  ("gis_lifts", "Лифты из ГИС ЖКХ");
        
        pk     ("uuid",                    Type.UUID,               "Ключ");
        
        ref    ("uuid_house_gis",          HouseGis.class,          "Дом");
        ref    ("fias_child_house_guid",   VocBuilding.class, null, "ГУИД дочернего дома по ФИАС, к которому относится подъезд для группирующих домов");
        
        col    ("entrance_num",            Type.STRING,  255, null, "Номер подъезда");
        col    ("factory_num",             Type.STRING,       null, "Заводской номер");
        col    ("code_vc_nsi_192",         Type.STRING,  20,  null, "Тип лифта (НСИ 192)");

        col    ("termination_date",        Type.DATE,         null, "Дата прекращения существования объекта");
        col    ("code_vc_nsi_330",         Type.STRING,  20,  null, "Причина аннулирования объекта жилищного фонда (НСИ 330)");
        col    ("annulment_info",          Type.STRING,       null, "Причина аннулирования. Дополнительная информация");
        
        col    ("gis_modification_date",   Type.DATETIME,     null, "Дата модификации данных в ГИС ЖКХ");
    }

}