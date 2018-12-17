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

public class Lift extends Table {

    public static final String YEAR_FIELD = "f_20164";
    
    public Lift () {
        
        super  ("tb_lifts", "Лифты");
        
        pk     ("uuid",               Type.UUID,   NEW_UUID,           "Ключ");
        
        ref    ("uuid_house",         House.class,                     "Дом");
        ref    ("uuid_entrance",      Entrance.class,                  "Подъезд");
        col    ("entrancenum",        Type.STRING,         null,       "Номер подъезда (Заполняется, если невозможно определить идентификатор подъезда)");
        
        col    ("factorynum",         Type.STRING,         null,       "Заводской номер");
        col    ("code_vc_nsi_192",    Type.STRING,  20,    null,       "Тип лифта");

        col    ("operatinglimit",     Type.NUMERIC, 4,     null,       "Предельный срок эксплуатации");
        
        col    ("f_20124",            Type.NUMERIC, 4,     null,       "Год проведения последнего капитального ремонта");
        col    (YEAR_FIELD,           Type.NUMERIC, 4,     null,       "Год ввода в эксплуатацию");
        col    ("f_20166",            Type.NUMERIC, 10,    null,       "Нормативный срок службы");        
        col    ("f_20007",            Type.STRING,         null,       "Инвентарный номер");       
        col    ("f_20165",            Type.NUMERIC, 19, 4, null,       "Грузоподъемность, кг");        
        col    ("f_20151",            Type.NUMERIC, 19, 4, null,       "Физический износ, лет");

        col    ("terminationdate",    Type.DATE,           null,       "Дата прекращения существования объекта");
        col    ("is_deleted",         Type.BOOLEAN,        Bool.FALSE, "1, если запись удалена; иначе 0");
        col    ("code_vc_nsi_330",    Type.STRING,  20,    null,       "Причина аннулирования");
        col    ("annulmentinfo",      Type.STRING,         null,       "Причина аннулирования. Дополнительная информация");
        
        col    ("annulmentreason",    Type.STRING,         new Virt ("''||\"CODE_VC_NSI_330\""),  "Причина аннулирования");
        col    ("is_annuled",         Type.BOOLEAN,        new Virt ("DECODE(\"CODE_VC_NSI_330\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");
        
        ref    ("fiaschildhouseguid",    VocBuilding.class, null,       "ГУИД дочернего дома по ФИАС, к которому относится подъезд для группирующих домов");
        col    ("gis_modification_date", Type.TIMESTAMP,    null,       "Дата модификации данных в ГИС ЖКХ");
        col    ("liftguid",              Type.UUID,         null,       "Идентификатор в ГИС ЖКХ");
        col    ("is_annuled_in_gis",     Type.BOOLEAN,      Bool.FALSE, "1, если запись аннулирована в ГИС ЖКХ; иначе 0");
        
        fk     ("id_status", VocHouseStatus.class, new Virt("DECODE(\"LIFTGUID\",NULL," + VocHouseStatus.i.MISSING.getId() + "," + VocHouseStatus.i.PUBLISHED.getId() + ")"), "Статус размещения в ГИС ЖКХ");
        
        trigger ("BEFORE INSERT", 
            "BEGIN "
                    + "IF :NEW.uuid_entrance IS NOT NULL THEN"
                    + "  SELECT uuid_house INTO :NEW.uuid_house FROM tb_entrances WHERE uuid = :NEW.uuid_entrance; "
                    + "END IF;"
            + "END;");
        
        trigger ("BEFORE UPDATE", ""
                
        + "BEGIN "

            + "IF :OLD.code_vc_nsi_330 IS NOT NULL AND :NEW.code_vc_nsi_330 IS NULL THEN " // отмена аннулирования — проверка подъезда
            + " FOR i IN (SELECT uuid FROM tb_entrances WHERE uuid=:NEW.uuid_entrance AND code_vc_nsi_330 IS NOT NULL) LOOP"
            + "   raise_application_error (-20000, 'Данная запись о лифте относится к аннулированной записи о подъезде. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "

        + "END;");

    }
    
    public static void add(ImportHouseUORequest.ApartmentHouse house, Map<String, Object> r) {
        if (r.get("liftguid") == null) {
            ImportHouseUORequest.ApartmentHouse.LiftToCreate lift = TypeConverter.javaBean(ImportHouseUORequest.ApartmentHouse.LiftToCreate.class, r);
            house.getLiftToCreate().add(lift);
        } else {
            ImportHouseUORequest.ApartmentHouse.LiftToUpdate lift = TypeConverter.javaBean(ImportHouseUORequest.ApartmentHouse.LiftToUpdate.class, r);
            house.getLiftToUpdate().add(lift);
        }
    }
    
    public static void add(ImportHouseOMSRequest.ApartmentHouse house, Map<String, Object> r) {
        if (r.get("liftguid") == null) {
            ImportHouseOMSRequest.ApartmentHouse.LiftToCreate lift = TypeConverter.javaBean(ImportHouseOMSRequest.ApartmentHouse.LiftToCreate.class, r);
            house.getLiftToCreate().add(lift);
        } else {
            ImportHouseOMSRequest.ApartmentHouse.LiftToUpdate lift = TypeConverter.javaBean(ImportHouseOMSRequest.ApartmentHouse.LiftToUpdate.class, r);
            house.getLiftToUpdate().add(lift);
        }
    }
    
    public static void add(ImportHouseESPRequest.ApartmentHouse house, Map<String, Object> r) {
        if (r.get("liftguid") == null) {
            ImportHouseESPRequest.ApartmentHouse.LiftToCreate lift = TypeConverter.javaBean(ImportHouseESPRequest.ApartmentHouse.LiftToCreate.class, r);
            house.getLiftToCreate().add(lift);
        } else {
            ImportHouseESPRequest.ApartmentHouse.LiftToUpdate lift = TypeConverter.javaBean(ImportHouseESPRequest.ApartmentHouse.LiftToUpdate.class, r);
            house.getLiftToUpdate().add(lift);
        }
    }

}