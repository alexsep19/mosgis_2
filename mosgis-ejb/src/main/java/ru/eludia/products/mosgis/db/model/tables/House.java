package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import java.sql.SQLException;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;

public class House extends Passport {
    
    public static final String TABLE_NAME = "tb_houses";
    
    public House () {
        
        super  (TABLE_NAME,             "МКД/ЖД");
        
        pk     ("uuid",                  Type.UUID,   NEW_UUID,            "Ключ");
        col    ("unom",                  Type.NUMERIC, 12, null,           "UNOM (код дома в московских ИС)");
        
        fk     ("fiashouseguid",         VocBuilding.class, null,          "Глобальный уникальный идентификатор дома по ФИАС");
        fk     ("id_status",             VocGisStatus.class,               "Статус");

        col    ("address",               Type.STRING,                      "Адрес");
        col    ("address_uc",            Type.STRING,  new Virt ("UPPER(\"ADDRESS\")"),  "АДРЕС В ВЕРХНЕМ РЕГИСТРЕ");

        col    ("totalsquare",           Type.NUMERIC, 25, 4, null,        "Общая площадь");
        col    ("usedyear",              Type.NUMERIC,  4, null,           "Год ввода в эксплуатацию");
        col    ("culturalheritage",      Type.BOOLEAN,     Bool.FALSE,     "Наличие у дома статуса объекта культурного наследия");
        col    ("floorcount",            Type.NUMERIC,  3, null,           "Количество этажей");
        col    ("minfloorcount",         Type.NUMERIC,  3, null,           "Количество этажей, наименьшее");
        col    ("undergroundfloorcount", Type.NUMERIC,  3, null,           "Количество подземных этажей");

        col    ("is_condo",              Type.BOOLEAN, null,                "1 для МКД, 0 для ЖД");
        col    ("hasblocks",                      Type.BOOLEAN, Bool.FALSE, "1 для жилых домов блокированной застройки, иначе 0");
        col    ("hasmultiplehouseswithsameadres", Type.BOOLEAN, Bool.FALSE, "1, если есть несколько жилых домов с одинаковым адресом, иначе 0");
        
        col    ("id_vc_rd_1240",         Type.INTEGER, null,               "Вид жилого фонда");
        
        col    ("code_vc_nsi_24",        Type.STRING, 20, null,            "Состояние дома");
        col    ("kad_n",                 Type.STRING, null,                "Кадастровый номер");
        
        col    ("gis_unique_number",     Type.STRING,         null,       "Уникальный номер дома из ГИС ЖКХ");
        col    ("gis_modification_date", Type.TIMESTAMP,      null,       "Дата модификации данных дома в ГИС ЖКХ");
        col    ("terminationdate",       Type.DATE,           null,       "Дата аннулирования объекта в ГИС ЖКХ");
        col    ("code_vc_nsi_330",       Type.STRING,  20,    null,       "Причина аннулирования объекта жилищного фонда (НСИ 330)");
        col    ("annulmentinfo",         Type.STRING,         null,       "Причина аннулирования.Дополнительная информация");
        col    ("code_vc_nsi_241",       Type.STRING,  20,    null,       "Способ формирования фонда капитального ремонта (НСИ 241)");
        col    ("code_vc_nsi_25",        Type.STRING,  20,    null,       "Способ управления домом (НСИ 25)");
        col    ("code_vc_nsi_336",       Type.STRING,  20,    null,       "Стадия жизненного цикла (НСИ 336)");
        
        col    ("ts",                    Type.TIMESTAMP,                   "Дата/время последнего изменения в БД");
//        fk     ("uuid_in_open_data",     InOpenData.class,                 "Последний пакет импорта");

        key    ("fiashouseguid1", "fiashouseguid");

        key    ("address1", "address");
        key    ("unom", "unom");

        trigger ("BEFORE INSERT OR UPDATE", "BEGIN :NEW.ts := CURRENT_TIMESTAMP(); END;");

    }

    @Override
    public void addNsiFields (DB db) throws SQLException {
        
        boolean isVirgin = refTables.isEmpty ();

            db.forEach (model.select (VocPassportFields.class, "*"), rs -> {

                if (rs.getInt ("is_for_condo") != 1 && rs.getInt ("is_for_cottage") != 1) return;
                
                if (rs.getInt ("is_multiple") == 1) {
                    
                    if (!isVirgin) return;

                    MultipleRefTable refTable = new MultipleRefTable (this, rs.getString ("id"), remark + ": " + rs.getString ("label"));
                    
                    db.adjustTable (refTable);
                    
                    refTables.add (refTable);
                    
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