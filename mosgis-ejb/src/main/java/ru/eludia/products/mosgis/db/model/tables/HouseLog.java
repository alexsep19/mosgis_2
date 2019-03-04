package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import java.sql.SQLException;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocHouseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class HouseLog extends Passport {
        
    public HouseLog () {
        
        super  ("tb_houses__log",             "История изменения МКД/ЖД");
        
        pk    ("uuid",          Type.UUID,             NEW_UUID, "Ключ");
        ref   ("action",        VocAction.class,                 "Действие");
        fk    ("uuid_object",   House.class,                     "Ссылка на запись");
        col   ("ts",            Type.TIMESTAMP,        NOW,      "Дата/время события");
        fk    ("uuid_org",      VocOrganization.class, null,     "Организация");
        fk    ("uuid_user",     VocUser.class,         null,     "Оператор");
        fk    ("uuid_out_soap", OutSoap.class,         null,     "Последний запрос на импорт в ГИС ЖКХ");
        col   ("uuid_message",  Type.UUID,             null,     "UUID запроса в ГИС ЖКХ");
        
        
        col    ("unom",                  Type.NUMERIC,      12,    null, "UNOM (код дома в московских ИС)");
        fk     ("fiashouseguid",         VocBuilding.class,        null, "Глобальный уникальный идентификатор дома по ФИАС");
        fk     ("id_status_gis",         VocGisStatus.class,       null, "Статус обмена с ГИС ЖКХ");
        fk     ("id_status",             VocHouseStatus.class,     null, "Статус размещения в ГИС ЖКХ");
        col    ("address",               Type.STRING,              null, "Адрес");
        col    ("totalsquare",           Type.NUMERIC,      25, 4, null, "Общая площадь");
        col    ("usedyear",              Type.NUMERIC,      4,     null, "Год ввода в эксплуатацию");
        col    ("culturalheritage",      Type.BOOLEAN,             null, "Наличие у дома статуса объекта культурного наследия");
        col    ("floorcount",            Type.NUMERIC,      3,     null, "Количество этажей");
        col    ("minfloorcount",         Type.NUMERIC,      3,     null, "Количество этажей, наименьшее");
        col    ("undergroundfloorcount", Type.NUMERIC,      3,     null, "Количество подземных этажей");
        col    ("is_condo",              Type.BOOLEAN,             null, "1 для МКД, 0 для ЖД");
        col    ("hasblocks",             Type.BOOLEAN,             null, "1 для жилых домов блокированной застройки, иначе 0");
        col    ("id_vc_rd_1240",         Type.INTEGER,             null, "Вид жилого фонда");
        col    ("code_vc_nsi_24",        Type.STRING,       20,    null, "Состояние дома");
        col    ("kad_n",                 Type.STRING,              null, "Кадастровый номер");
        col    ("terminationdate",       Type.DATE,                null, "Дата аннулирования объекта в ГИС ЖКХ");
        col    ("code_vc_nsi_330",       Type.STRING,       20,    null, "Причина аннулирования объекта жилищного фонда (НСИ 330)");
        col    ("annulmentinfo",         Type.STRING,              null, "Причина аннулирования.Дополнительная информация");
        col    ("code_vc_nsi_241",       Type.STRING,       20,    null, "Способ формирования фонда капитального ремонта (НСИ 241)");
        col    ("code_vc_nsi_25",        Type.STRING,       20,    null, "Способ управления домом (НСИ 25)");
        col    ("code_vc_nsi_338",       Type.STRING,       20,    null, "Стадия жизненного цикла (НСИ 336)");
        col    ("hasmultiplehouseswithsameadres", Type.BOOLEAN, Bool.FALSE, "1, если есть несколько жилых домов с одинаковым адресом, иначе 0");
        
        col    ("f_20819",               Type.BOOLEAN, null,       "Наличие подземного паркинга");
        col    ("f_20140",               Type.NUMERIC, 10, null,   "Количество проживающих");
        

        trigger ("BEFORE INSERT", "BEGIN "

            + "SELECT"
            + "    unom"
            + "    , fiashouseguid"
            + "    , id_status_gis" 
            + "    , id_status"   
            + "    , address"
            + "    , totalsquare"
            + "    , usedyear"
            + "    , culturalheritage"
            + "    , floorcount"
            + "    , minfloorcount"
            + "    , undergroundfloorcount"
            + "    , is_condo"
            + "    , hasblocks"
            + "    , id_vc_rd_1240"
            + "    , code_vc_nsi_24"
            + "    , kad_n"
            + "    , terminationdate"
            + "    , code_vc_nsi_330"
            + "    , annulmentinfo"
            + "    , code_vc_nsi_241"
            + "    , code_vc_nsi_25"
            + "    , code_vc_nsi_338"
            + "    , hasmultiplehouseswithsameadres"
            + "    , f_20819"
            + "    , f_20140"
            + " INTO"
            + "    :NEW.unom"
            + "    , :NEW.fiashouseguid"
            + "    , :NEW.id_status_gis"
            + "    , :NEW.id_status"
            + "    , :NEW.address"
            + "    , :NEW.totalsquare"
            + "    , :NEW.usedyear"
            + "    , :NEW.culturalheritage"
            + "    , :NEW.floorcount"
            + "    , :NEW.minfloorcount"
            + "    , :NEW.undergroundfloorcount"
            + "    , :NEW.is_condo"
            + "    , :NEW.hasblocks"
            + "    , :NEW.id_vc_rd_1240"
            + "    , :NEW.code_vc_nsi_24"
            + "    , :NEW.kad_n"
            + "    , :NEW.terminationdate"
            + "    , :NEW.code_vc_nsi_330"
            + "    , :NEW.annulmentinfo"
            + "    , :NEW.code_vc_nsi_241"
            + "    , :NEW.code_vc_nsi_25"
            + "    , :NEW.code_vc_nsi_338"
            + "    , :NEW.hasmultiplehouseswithsameadres"
            + "    , :NEW.f_20819"
            + "    , :NEW.f_20140"
            + " FROM tb_houses WHERE uuid=:NEW.uuid_object; "

       + "END;");   
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