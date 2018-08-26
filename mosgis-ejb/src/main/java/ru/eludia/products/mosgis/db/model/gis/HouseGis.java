package ru.eludia.products.mosgis.db.model.gis;

import java.sql.SQLException;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.products.mosgis.db.model.tables.Passport;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;

public class HouseGis extends Passport {
    
    public static final String TABLE_NAME = "gis_houses";

    public HouseGis () {
        
        super  (TABLE_NAME,             "МКД/ЖД из ГИС ЖКХ");
        
        pk     ("fias_house_guid",         Type.UUID,                      "Глобальный уникальный идентификатор дома по ФИАС");
        
        col    ("unique_number",           Type.STRING,                     "Уникальный номер дома");
        col    ("gis_modification_date",   Type.DATETIME,       null,       "Дата модификации данных дома в ГИС ЖКХ");
        col    ("cadastral_number",        Type.STRING,         null,       "Кадастровый номер");
        
        col    ("is_condo",                Type.BOOLEAN,        null,       "1 для МКД, 0 для ЖД");
        col    ("has_blocks",              Type.BOOLEAN,        Bool.FALSE, "1 для жилых домов блокированной застройки, иначе 0");
        col    ("total_square",            Type.NUMERIC, 25, 4, null,       "Общая площадь");
        col    ("code_vc_nsi_24",          Type.STRING,  20,    null,       "Состояние (НСИ 24)");
        col    ("used_year",               Type.NUMERIC, 4,     null,       "Год ввода в эксплуатацию");
        col    ("floor_count",             Type.STRING,  25, 4, null,       "Количество этажей");
        col    ("min_floor_count",         Type.NUMERIC, 3,     null,       "Количество этажей, наименьшее");
        col    ("underground_floor_count", Type.STRING,         null,       "Количество подземных этажей");
        col    ("oktmo",                   Type.STRING,  11,    null,       "ОКТМО");
        col    ("code_vc_nsi_32",          Type.STRING,  20,    null,       "Часовая зона");
        col    ("cultural_heritage",       Type.BOOLEAN,        Bool.FALSE, "Наличие у дома статуса объекта культурного наследия");
        col    ("termination_date",        Type.DATE,           null,       "Дата аннулирования объекта в ГИС ЖКХ");
        col    ("code_vc_nsi_330",         Type.STRING,  20,    null,       "Причина аннулирования объекта жилищного фонда (НСИ 330)");
        col    ("annulment_info",          Type.STRING,         null,       "Причина аннулирования.Дополнительная информация");
        col    ("code_vc_nsi_241",         Type.STRING,  20,    null,       "Способ формирования фонда капитального ремонта (НСИ 241)");
        col    ("code_vc_nsi_25",          Type.STRING,  20,    null,       "Способ управления домом (НСИ 25)");
        
        col    ("ts",                      Type.TIMESTAMP,                  "Дата/время последнего изменения в БД");

        col    ("has_multi_houses_with_same_adr", Type.BOOLEAN, Bool.FALSE, "1, если есть несколько жилых домов с одинаковым адресом, иначе 0");

        trigger("BEFORE INSERT OR UPDATE", "BEGIN :NEW.ts := CURRENT_TIMESTAMP(); END;");
    }
    
    @Override
    public void addNsiFields(DB db) throws SQLException {
        boolean isVirgin = getRefTables().isEmpty ();

            db.forEach (model.select (VocPassportFields.class, "*"), rs -> {

                if (rs.getInt ("is_for_condo") != 1 && rs.getInt ("is_for_cottage") != 1) return;
                
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