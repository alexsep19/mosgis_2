package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocHouseStatus;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseESPRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseOMSRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseUORequest;

public class Entrance extends Table {

    public Entrance () {
        
        super ("tb_entrances", "Подъезды");
        
        pk     ("uuid",               Type.UUID,   NEW_UUID, "Ключ");
        
        ref    ("uuid_house",         House.class, "Дом");
        
        col    ("entrancenum",        Type.STRING,  255,        "Номер подъезда");
        col    ("storeyscount",       Type.NUMERIC,   2,  null, "Количество этажей");
        col    ("creationyear",       Type.NUMERIC,   4,  null, "Год постройки");
        
        col    ("is_deleted",         Type.BOOLEAN,        Bool.FALSE, "1, если запись удалена; иначе 0");

        col    ("terminationdate",    Type.DATE,           null,       "Дата прекращения существования объекта");
        col    ("code_vc_nsi_330",    Type.STRING,  20,    null,       "Причина аннулирования");
        col    ("annulmentinfo",      Type.STRING,         null,       "Причина аннулирования. Дополнительная информация");

        col    ("annulmentreason",    Type.STRING,         new Virt ("''||\"CODE_VC_NSI_330\""),  "Причина аннулирования");
        col    ("is_annuled",         Type.BOOLEAN,        new Virt ("DECODE(\"CODE_VC_NSI_330\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");

        ref    ("fiaschildhouseguid",    VocBuilding.class, null,       "ГУИД дочернего дома по ФИАС, к которому относится подъезд для группирующих домов");
        col    ("gis_modification_date", Type.TIMESTAMP,    null,       "Дата модификации данных подъезда в ГИС ЖКХ");
        col    ("informationconfirmed",  Type.BOOLEAN,      Bool.TRUE,  "Информация подтверждена поставщиком");
        col    ("entranceguid",          Type.UUID,         null,       "Идентификатор в ГИС ЖКХ");
        col    ("is_annuled_in_gis",     Type.BOOLEAN,      Bool.FALSE, "1, если запись аннулирована в ГИС ЖКХ; иначе 0");
        
        fk     ("id_status", VocHouseStatus.class, new Virt("DECODE(\"ENTRANCEGUID\",NULL," + VocHouseStatus.i.MISSING.getId() + "," + VocHouseStatus.i.PUBLISHED.getId() + ")"), "Статус размещения в ГИС ЖКХ");
        
        trigger ("BEFORE INSERT", ""
            
            + "DECLARE "
                + "PRAGMA AUTONOMOUS_TRANSACTION; "
                
            + "BEGIN "
        
            + "FOR i IN (SELECT entrancenum, is_annuled_in_gis FROM tb_entrances WHERE uuid_house = :NEW.uuid_house AND entrancenum = :NEW.entrancenum AND is_deleted = 0) LOOP "
                + "IF (i.is_annuled_in_gis <> 1) THEN "
                    + "raise_application_error (-20000, 'Аннулирование записи подъезда №' || i.entrancenum || ' не подтверждено в ГИС. Операция отменена.'); "
                + "END IF; "
            + "END LOOP; "
            
            + "END;"
        );
        
        trigger ("BEFORE UPDATE", ""
                
            + "BEGIN "
                
            + "IF :OLD.is_deleted = 0 AND :NEW.is_deleted = 1 THEN " // удаление — проверка на дочерние 
            + " FOR i IN (SELECT uuid FROM tb_premises_res WHERE is_deleted=0 AND uuid_entrance=:NEW.uuid) LOOP"
            + "   raise_application_error (-20000, 'К подъезду №' || :NEW.entrancenum || ' привязаны помещения. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "
                
            + "IF :NEW.creationyear > 0 THEN " // год — сотоставление с лифтами
            + " FOR i IN (SELECT factorynum no, " + Lift.YEAR_FIELD + " year FROM tb_lifts WHERE is_deleted=0 AND uuid_entrance=:NEW.uuid AND " + Lift.YEAR_FIELD + " < :NEW.creationyear) LOOP"
            + "   raise_application_error (-20000, 'По имеющимся сведениям, лифт №' || i.no || ' введён в эксплуатацию в ' || i.year || ' г. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "

            + "IF :OLD.code_vc_nsi_330 IS NULL AND :NEW.code_vc_nsi_330 IS NOT NULL THEN " // аннулирование — проверка помещений
            + " FOR i IN (SELECT uuid, premisesnum FROM tb_premises_res WHERE is_deleted=0 AND code_vc_nsi_330 IS NULL AND uuid_entrance=:NEW.uuid) LOOP"
            + "   raise_application_error (-20000, 'К данному подъезду относится помещение №' || i.premisesnum || '. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "
                    
            + "IF :OLD.code_vc_nsi_330 IS NULL AND :NEW.code_vc_nsi_330 IS NOT NULL THEN " // аннулирование — проверка лифтов
            + " FOR i IN (SELECT uuid, factorynum FROM tb_lifts WHERE is_deleted=0 AND code_vc_nsi_330 IS NULL AND uuid_entrance=:NEW.uuid) LOOP"
            + "   raise_application_error (-20000, 'К данному подъезду относится лифт №' || i.factorynum || '. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "
/*                    
            + "IF :OLD.code_vc_nsi_330 IS NOT NULL AND :NEW.code_vc_nsi_330 IS NULL THEN " // отмена аннулирования — проверка дома
            + " FOR i IN (SELECT uuid FROM tb_houses WHERE uuid=:NEW.uuid_house AND code_vc_nsi_330 IS NOT NULL) LOOP"
            + "   raise_application_error (-20000, 'Данная запись о подъезде относится к аннулированной записи о доме. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "
*/
        + "END;");
        
    }
    
    public static void add(ImportHouseUORequest.ApartmentHouse house, Map<String, Object> r) {
        if (r.get("entranceguid") == null) {
            ImportHouseUORequest.ApartmentHouse.EntranceToCreate entrance = TypeConverter.javaBean(ImportHouseUORequest.ApartmentHouse.EntranceToCreate.class, r);
            house.getEntranceToCreate().add(entrance);
        } else {
            ImportHouseUORequest.ApartmentHouse.EntranceToUpdate entrance = TypeConverter.javaBean(ImportHouseUORequest.ApartmentHouse.EntranceToUpdate.class, r);
            house.getEntranceToUpdate().add(entrance);
        }
    }
    
    public static void add(ImportHouseOMSRequest.ApartmentHouse house, Map<String, Object> r) {
        if (r.get("entranceguid") == null) {
            ImportHouseOMSRequest.ApartmentHouse.EntranceToCreate entrance = TypeConverter.javaBean(ImportHouseOMSRequest.ApartmentHouse.EntranceToCreate.class, r);
            house.getEntranceToCreate().add(entrance);
        } else {
            ImportHouseOMSRequest.ApartmentHouse.EntranceToUpdate entrance = TypeConverter.javaBean(ImportHouseOMSRequest.ApartmentHouse.EntranceToUpdate.class, r);
            house.getEntranceToUpdate().add(entrance);
        }
    }
    
    public static void add(ImportHouseRSORequest.ApartmentHouse house, Map<String, Object> r) {
        if (r.get("entranceguid") == null) {
            ImportHouseRSORequest.ApartmentHouse.EntranceToCreate entrance = TypeConverter.javaBean(ImportHouseRSORequest.ApartmentHouse.EntranceToCreate.class, r);
            house.getEntranceToCreate().add(entrance);
        } else {
            ImportHouseRSORequest.ApartmentHouse.EntranceToUpdate entrance = TypeConverter.javaBean(ImportHouseRSORequest.ApartmentHouse.EntranceToUpdate.class, r);
            house.getEntranceToUpdate().add(entrance);
        }
    }
    
    public static void add(ImportHouseESPRequest.ApartmentHouse house, Map<String, Object> r) {
        if (r.get("entranceguid") == null) {
            ImportHouseESPRequest.ApartmentHouse.EntranceToCreate entrance = TypeConverter.javaBean(ImportHouseESPRequest.ApartmentHouse.EntranceToCreate.class, r);
            house.getEntranceToCreate().add(entrance);
        } else {
            ImportHouseESPRequest.ApartmentHouse.EntranceToUpdate entrance = TypeConverter.javaBean(ImportHouseESPRequest.ApartmentHouse.EntranceToUpdate.class, r);
            house.getEntranceToUpdate().add(entrance);
        }
    }
    
}