package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;

public class LivingRoom extends Passport {
    
    public LivingRoom () {
        
        super  ("tb_living_rooms", "Комнаты в квартирах коммунального заселения");
        
        pk     ("uuid",               Type.UUID,   NEW_UUID,           "Ключ");
        col    ("terminationdate",    Type.DATE,           null,       "Дата прекращения существования объекта");
        col    ("is_deleted",         Type.BOOLEAN,        Bool.FALSE, "1, если запись удалена; иначе 0");
        col    ("code_vc_nsi_330",    Type.STRING,  20,    null,       "Причина аннулирования");
        col    ("annulmentinfo",      Type.STRING,         null,       "Причина аннулирования. Дополнительная информация");
        
        col    ("annulmentreason",    Type.STRING,         new Virt ("''||\"CODE_VC_NSI_330\""),  "Причина аннулирования");
        col    ("is_annuled",         Type.BOOLEAN,        new Virt ("DECODE(\"CODE_VC_NSI_330\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");
        
        ref    ("uuid_house",         House.class,                     "Дом");
        ref    ("uuid_premise",       ResidentialPremise.class, null,  "Квартира");
        ref    ("uuid_block",         Block.class,              null,  "Блок");
        
        col    ("roomnumber",         Type.STRING, 255,    null,       "Номер комнаты");
        col    ("cadastralnumber",    Type.STRING,         null,       "Кадастровый номер");
        col    ("square",             Type.NUMERIC, 25, 4, null,       "Площадь");
        col    ("floor",              Type.STRING,         null,       "Этаж");
        
        col    ("gis_unique_number",       Type.STRING,    null,       "Уникальный номер");
        col    ("gis_modification_date",   Type.TIMESTAMP, null,       "Дата модификации данных в ГИС ЖКХ");
        col    ("information_confirmed",   Type.BOOLEAN,   Bool.TRUE,  "Информация подтверждена поставщиком");
        col    ("guid_gis",                Type.UUID,      null,       "Идентификатор в ГИС ЖКХ");
        
        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "
                
            + " IF INSERTING THEN "
                
            + "  IF :NEW.uuid_premise IS NOT NULL THEN "
            + "   SELECT uuid_house INTO :NEW.uuid_house FROM tb_premises_res WHERE uuid = :NEW.uuid_premise; "
            + "  END IF;"
                
            + "  IF :NEW.uuid_block IS NOT NULL THEN "
            + "   SELECT uuid_house INTO :NEW.uuid_house FROM tb_blocks WHERE uuid = :NEW.uuid_block; "
            + "  END IF;"
                
            + " END IF;"

            + "IF :NEW.roomnumber IS NULL THEN raise_application_error (-20000, '#roomnumber#: Необходимо указать номер комнаты.'); END IF; "                

            + " IF UPDATING THEN "

                + "IF :OLD.code_vc_nsi_330 IS NOT NULL AND :NEW.code_vc_nsi_330 IS NULL THEN " // отмена аннулирования — проверка помещения
                + " FOR i IN (SELECT uuid, premisesnum FROM tb_premises_res WHERE is_deleted=0 AND code_vc_nsi_330 IS NOT NULL AND uuid=:NEW.uuid_premise) LOOP"
                + "   raise_application_error (-20000, 'В настоящий момент помещение №' || i.premisesnum || ', к которому относится комната №' || :NEW.roomnumber || ', аннулировано. Операция отменена.'); "
                + " END LOOP; "
                + "END IF; "

                + "IF :OLD.code_vc_nsi_330 IS NOT NULL AND :NEW.code_vc_nsi_330 IS NULL THEN " // отмена аннулирования — проверка блока
                + " FOR i IN (SELECT uuid, blocknum FROM tb_blocks WHERE is_deleted=0 AND code_vc_nsi_330 IS NOT NULL AND uuid=:NEW.uuid_block) LOOP"
                + "   raise_application_error (-20000, 'В настоящий момент блок №' || i.blocknum || ', к которому относится комната №' || :NEW.roomnumber || ', аннулирован. Операция отменена.'); "
                + " END LOOP; "
                + "END IF; "
                
            + " END IF;"
                
        + "END;");
        
    }
        
    @Override
    public void addNsiFields (DB db) throws SQLException {

        boolean isVirgin = refTables.isEmpty ();
        
            db.forEach (model.select (VocPassportFields.class, "*").where ("is_for_living_room", 1).and ("id_type IS NOT NULL"), rs -> {

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