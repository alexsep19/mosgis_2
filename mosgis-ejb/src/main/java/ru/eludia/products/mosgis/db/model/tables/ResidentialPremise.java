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

public class ResidentialPremise extends Passport {
    
    public ResidentialPremise () {
        
        super  ("tb_premises_res", "Жилые помещения");
        
        pk     ("uuid",               Type.UUID,   NEW_UUID,           "Ключ");
        col    ("terminationdate",    Type.DATE,           null,       "Дата прекращения существования объекта");
        col    ("is_deleted",         Type.BOOLEAN,        Bool.FALSE, "1, если запись удалена; иначе 0");
        col    ("code_vc_nsi_330",    Type.STRING,  20,    null,       "Причина аннулирования");
        col    ("annulmentinfo",      Type.STRING,         null,       "Причина аннулирования. Дополнительная информация");
        
        col    ("annulmentreason",    Type.STRING,         new Virt ("''||\"CODE_VC_NSI_330\""),  "Причина аннулирования");
        col    ("is_annuled",         Type.BOOLEAN,        new Virt ("DECODE(\"CODE_VC_NSI_330\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");
        
        ref    ("uuid_house",         House.class,                     "Дом");
        ref    ("uuid_entrance",      Entrance.class,      null,       "Подъезд");
        
        col    ("premisesnum",        Type.STRING, 255,    null,       "Номер помещения");
        col    ("cadastralnumber",    Type.STRING,         null,       "Кадастровый номер");
        col    ("code_vc_nsi_30",     Type.STRING,  20,    null,       "Характеристика помещения");

        col    ("totalarea",          Type.NUMERIC, 25, 4, null,       "Общая площадь жилого помещения");
        col    ("grossarea",          Type.NUMERIC, 25, 4, null,       "Жилая площадь жилого помещения");
        col    ("f_20002",            Type.INTEGER,        null,       "Количество комнат");
        col    ("floor",              Type.STRING,         null,       "Этаж");
        
        col    ("gis_unique_number",       Type.STRING,    null,       "Уникальный номер");
        col    ("gis_modification_date",   Type.TIMESTAMP, null,       "Дата модификации данных в ГИС ЖКХ");
        col    ("information_confirmed",   Type.BOOLEAN,   Bool.TRUE,  "Информация подтверждена поставщиком");
        col    ("guid_gis",                Type.UUID,      null,       "Идентификатор в ГИС ЖКХ");
        
        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "
                
            + "IF :NEW.totalarea < :NEW.grossarea THEN raise_application_error (-20000, '#grossarea#: Жилая площадь не может превышать общую.'); END IF; "
                
            + "IF UPDATING THEN "
                + "IF :NEW.premisesnum IS NULL      AND :OLD.premisesnum IS NOT NULL THEN raise_application_error (-20000, '#premisesnum#: Необходимо указать номер помещения.'); END IF; "
                + "IF :NEW.code_vc_nsi_30 IS NULL   AND :OLD.code_vc_nsi_30 IS NOT NULL  THEN raise_application_error (-20000, '#code_vc_nsi_30#: Необходимо указать характеристику помещения.'); END IF; "
                + "IF  NVL (:NEW.totalarea, 0) <= 0 AND :OLD.totalarea > 0  THEN raise_application_error (-20000, '#totalarea#: Необходимо указать размер общей плошади.'); END IF; "
                + "IF  NVL (:NEW.grossarea, 0) <= 0 AND :OLD.grossarea > 0  THEN raise_application_error (-20000, '#grossarea#: Необходимо указать размер жилой плошади.'); END IF; "
                + "IF :NEW.f_20002 IS NULL          AND :OLD.f_20002 IS NOT NULL THEN raise_application_error (-20000, '#f_20002#: Необходимо указать количество комнат.'); END IF; "

                + "IF :OLD.code_vc_nsi_30 = '2' AND NVL (:NEW.code_vc_nsi_30, '0') <> '2' THEN " // смена категории с коммуналки на другую — проверка на дочерние комнаты
                + " FOR i IN (SELECT uuid FROM tb_living_rooms WHERE is_deleted=0 AND code_vc_nsi_330 IS NULL AND uuid_premise=:NEW.uuid) LOOP"
                + "   raise_application_error (-20000, 'К помещению №' || :NEW.premisesnum || ' привязаны комнаты, следовательно, категория не может быть изменена. Операция отменена.'); "
                + " END LOOP; "
                + "END IF; "
                
                + "IF ((:NEW.is_deleted - :OLD.is_deleted) = 1) OR (:OLD.code_vc_nsi_330 IS NULL AND :NEW.code_vc_nsi_330 IS NOT NULL) THEN " // удаление или аннулирование — проверка на дочерние комнаты
                + " FOR i IN (SELECT uuid FROM tb_living_rooms WHERE is_deleted=0 AND code_vc_nsi_330 IS NULL AND uuid_premise=:NEW.uuid) LOOP"
                + "   raise_application_error (-20000, 'К помещению №' || :NEW.premisesnum || ' привязаны комнаты. Операция отменена.'); "
                + " END LOOP; "
                + "END IF; "
                
                + "IF :OLD.code_vc_nsi_330 IS NOT NULL AND :NEW.code_vc_nsi_330 IS NULL THEN " // отмена аннулирования — проверка подъезда
                + " FOR i IN (SELECT uuid FROM tb_entrances WHERE uuid=:NEW.uuid_entrance AND code_vc_nsi_330 IS NOT NULL) LOOP"
                + "   raise_application_error (-20000, 'Данная запись о помещении относится к аннулированной записи о подъезде. Операция отменена.'); "
                + " END LOOP; "
                + "END IF; "
                
/*                
                + "IF :OLD.code_vc_nsi_330 IS NOT NULL AND :NEW.code_vc_nsi_330 IS NULL THEN " // отмена аннулирования — проверка дома
                + " FOR i IN (SELECT uuid FROM tb_houses WHERE uuid=:NEW.uuid_house AND code_vc_nsi_330 IS NOT NULL) LOOP"
                + "   raise_application_error (-20000, 'Данная запись о помещении относится к аннулированной записи о доме. Операция отменена.'); "
                + " END LOOP; "
                + "END IF; "
*/                
            + "END IF;"
                
        + "END;");

    }
        
    @Override
    public void addNsiFields (DB db) throws SQLException {

        boolean isVirgin = refTables.isEmpty ();
        
            db.forEach (model.select (VocPassportFields.class, "*").where ("is_for_premise_res", 1).and ("id_type IS NOT NULL"), rs -> {

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