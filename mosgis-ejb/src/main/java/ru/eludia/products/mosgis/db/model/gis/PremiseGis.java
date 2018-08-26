package ru.eludia.products.mosgis.db.model.gis;

import ru.eludia.products.mosgis.db.model.tables.*;
import java.sql.SQLException;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;

public class PremiseGis extends Passport {
    
    public PremiseGis () {
        
        super  ("gis_premises", "Помещения и комнаты");
        
        pk     ("uuid",                    Type.UUID,                       "Ключ");
        
        ref    ("uuid_house_gis",          HouseGis.class,                  "Дом");
        ref    ("fias_child_house_guid",   VocBuilding.class,   null,       "ГУИД дочернего дома по ФИАС, к которому относится подъезд для группирующих домов");
        
        col    ("code_vc_nsi_11",          Type.STRING,  20,    null,       "Категория помещения");
        col    ("code_vc_nsi_231",         Type.STRING,  20,    null,       "Вид жилого помещения");
        col    ("unique_number",           Type.STRING,                     "Уникальный номер");
        col    ("cadastral_number",        Type.STRING,  255,   null,       "Кадастровый номер");
        col    ("num",                     Type.STRING,  255,   null,       "Номер помещения");
        
        col    ("termination_date",        Type.DATE,           null,       "Дата аннулирования объекта в ГИС ЖКХ");
        col    ("code_vc_nsi_330",         Type.STRING,  20,    null,       "Причина аннулирования объекта жилищного фонда (НСИ 330)");
        col    ("annulment_info",          Type.STRING,         null,       "Причина аннулирования. Дополнительная информация");
        
        col    ("floor",                   Type.STRING,         null,       "Этаж");
        col    ("entrance_num",            Type.STRING,  255,   null,       "Номер подъезда");
        
        col    ("code_vc_nsi_30",          Type.STRING,  20,    null,       "Характеристика помещения");
        col    ("total_area",              Type.NUMERIC, 25, 4, null,       "Общая площадь помещения");
        col    ("gross_area",              Type.NUMERIC, 25, 4, null,       "Жилая площадь помещения");
        col    ("is_common_property",      Type.BOOLEAN,        Bool.FALSE, "Помещение, составляющее общее имущество в многоквартирном доме");
        
        col    ("gis_modification_date",   Type.DATETIME,       null,       "Дата модификации данных в ГИС ЖКХ");
        
        col    ("information_confirmed",   Type.BOOLEAN,        Bool.TRUE,  "Информация подтверждена поставщиком");
        
        ref    ("parent",                  PremiseGis.class,    null,       "Помещение, в котором находится комната");

    }
    
    //TODO Доработать под все типы помещений
    @Override
    public void addNsiFields (DB db) throws SQLException {

        boolean isVirgin = getRefTables().isEmpty ();

            db.forEach (model.select (VocPassportFields.class, "*").where ("is_for_premise_res", 1).and ("id_type IS NOT NULL"), rs -> {

                if (rs.getInt ("is_multiple") == 1) {
                    
                    if (!isVirgin) return;

                    MultipleRefTable refTable = new MultipleRefTable (this, rs.getString ("id"), remark + ": " + rs.getString ("label"));
                    
                    db.adjustTable (refTable);
                    
                    getRefTables().add (refTable);
                    
                }
                else {

                    Col col = VocRdColType.i.forId (rs.getInt ("id_type")).getColDef ("f_" + rs.getString ("id"), rs.getString ("label"));

                    if (col == null) return;
                    if (columns.containsKey (col.getName ())) return;

                    add (col);

                }

            });
            
            db.adjustTable (this);
            
    }
    
}