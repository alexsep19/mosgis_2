package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocHouseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseESPRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseOMSRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseUORequest;

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
        
        col    ("gis_unique_number",     Type.STRING,    null,       "Уникальный номер");
        col    ("gis_modification_date", Type.TIMESTAMP, null,       "Дата модификации данных в ГИС ЖКХ");
        col    ("informationconfirmed",  Type.BOOLEAN,   Bool.TRUE,  "Информация подтверждена поставщиком");
        col    ("livingroomguid",        Type.UUID,      null,       "Идентификатор в ГИС ЖКХ");
        col    ("is_annuled_in_gis",     Type.BOOLEAN,   Bool.FALSE, "1, если запись аннулирована в ГИС ЖКХ; иначе 0");
        
        fk     ("id_status", VocHouseStatus.class, new Virt("DECODE(\"LIVINGROOMGUID\",NULL," + VocHouseStatus.i.MISSING.getId() + "," + VocHouseStatus.i.PUBLISHED.getId() + ")"), "Статус размещения в ГИС ЖКХ");
        
        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "
                
            + " IF INSERTING THEN "
                
            + "  IF :NEW.uuid_premise IS NOT NULL THEN "
            + "   SELECT uuid_house INTO :NEW.uuid_house FROM tb_premises_res WHERE uuid = :NEW.uuid_premise; "
                    + "FOR i IN (SELECT roomnumber, is_annuled_in_gis FROM tb_living_rooms WHERE uuid_house = :NEW.uuid_house AND uuid_premise = :NEW.uuid_premise AND roomnumber = :NEW.roomnumber AND is_deleted = 0) LOOP "
                        + "IF (i.is_annuled_in_gis <> 1) THEN "
                            + "raise_application_error (-20000, 'Аннулирование записи комнаты с номером ' || i.roomnumber || ' не подтверждено в ГИС. Операция отменена.'); "
                        + "END IF; "
                    + "END LOOP; "
            + "  END IF;"
                
            + "  IF :NEW.uuid_block IS NOT NULL THEN "
            + "   SELECT uuid_house INTO :NEW.uuid_house FROM tb_blocks WHERE uuid = :NEW.uuid_block; "
                    + "FOR i IN (SELECT roomnumber, is_annuled_in_gis FROM tb_living_rooms WHERE uuid_house = :NEW.uuid_house AND uuid_block = :NEW.uuid_block AND roomnumber = :NEW.roomnumber AND is_deleted = 0) LOOP "
                        + "IF (i.is_annuled_in_gis <> 1) THEN "
                            + "raise_application_error (-20000, 'Аннулирование записи комнаты с номером ' || i.roomnumber || ' не подтверждено в ГИС. Операция отменена.'); "
                        + "END IF; "
                    + "END LOOP; "
            + "  END IF;"
                
            + " END IF;"

            + "IF :NEW.roomnumber IS NULL THEN raise_application_error (-20000, '#roomnumber#: Необходимо указать номер комнаты.'); END IF; "                

            + " IF UPDATING THEN "

                + "IF :NEW.is_deleted = 1 AND :OLD.is_deleted = 0 AND :OLD.id_status = " + VocHouseStatus.i.PUBLISHED.getId () + " THEN raise_application_error (-20000, 'Запись размещена в ГИС. Операция прервана.'); END IF; "
                
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
    
    public static void add(ImportHouseUORequest.LivingHouse house, Map<String, Object> r) {
        if (r.get("livingroomguid") == null) {
            ImportHouseUORequest.LivingHouse.LivingRoomToCreate livingRoom = TypeConverter.javaBean(ImportHouseUORequest.LivingHouse.LivingRoomToCreate.class, r);
            house.getLivingRoomToCreate().add(livingRoom);
        } else {
            ImportHouseUORequest.LivingHouse.LivingRoomToUpdate livingRoom = TypeConverter.javaBean(ImportHouseUORequest.LivingHouse.LivingRoomToUpdate.class, r);
            house.getLivingRoomToUpdate().add(livingRoom);
        }
    }
    
    public static void add(ImportHouseUORequest.LivingHouse.Blocks block, Map<String, Object> r) {
        if (r.get("livingroomguid") == null) {
            ImportHouseUORequest.LivingHouse.Blocks.LivingRoomToCreate livingRoom = TypeConverter.javaBean(ImportHouseUORequest.LivingHouse.Blocks.LivingRoomToCreate.class, r);
            block.getLivingRoomToCreate().add(livingRoom);
        } else {
            ImportHouseUORequest.LivingHouse.Blocks.LivingRoomToUpdate livingRoom = TypeConverter.javaBean(ImportHouseUORequest.LivingHouse.Blocks.LivingRoomToUpdate.class, r);
            block.getLivingRoomToUpdate().add(livingRoom);
        }
    }
    
    public static void add(ImportHouseUORequest.ApartmentHouse.ResidentialPremises premise, Map<String, Object> r) {
        if (r.get("livingroomguid") == null) {
            ImportHouseUORequest.ApartmentHouse.ResidentialPremises.LivingRoomToCreate livingRoom = TypeConverter.javaBean(ImportHouseUORequest.ApartmentHouse.ResidentialPremises.LivingRoomToCreate.class, r);
            premise.getLivingRoomToCreate().add(livingRoom);
        } else {
            ImportHouseUORequest.ApartmentHouse.ResidentialPremises.LivingRoomToUpdate livingRoom = TypeConverter.javaBean(ImportHouseUORequest.ApartmentHouse.ResidentialPremises.LivingRoomToUpdate.class, r);
            premise.getLivingRoomToUpdate().add(livingRoom);
        }
    }
    
    public static void add(ImportHouseOMSRequest.LivingHouse house, Map<String, Object> r) {
        if (r.get("livingroomguid") == null) {
            ImportHouseOMSRequest.LivingHouse.LivingRoomToCreate livingRoom = TypeConverter.javaBean(ImportHouseOMSRequest.LivingHouse.LivingRoomToCreate.class, r);
            house.getLivingRoomToCreate().add(livingRoom);
        } else {
            ImportHouseOMSRequest.LivingHouse.LivingRoomToUpdate livingRoom = TypeConverter.javaBean(ImportHouseOMSRequest.LivingHouse.LivingRoomToUpdate.class, r);
            house.getLivingRoomToUpdate().add(livingRoom);
        }
    }
    
    public static void add(ImportHouseOMSRequest.LivingHouse.Blocks block, Map<String, Object> r) {
        if (r.get("livingroomguid") == null) {
            ImportHouseOMSRequest.LivingHouse.Blocks.LivingRoomToCreate livingRoom = TypeConverter.javaBean(ImportHouseOMSRequest.LivingHouse.Blocks.LivingRoomToCreate.class, r);
            block.getLivingRoomToCreate().add(livingRoom);
        } else {
            ImportHouseOMSRequest.LivingHouse.Blocks.LivingRoomToUpdate livingRoom = TypeConverter.javaBean(ImportHouseOMSRequest.LivingHouse.Blocks.LivingRoomToUpdate.class, r);
            block.getLivingRoomToUpdate().add(livingRoom);
        }
    }
    
    public static void add(ImportHouseOMSRequest.ApartmentHouse.ResidentialPremises premise, Map<String, Object> r) {
        if (r.get("livingroomguid") == null) {
            ImportHouseOMSRequest.ApartmentHouse.ResidentialPremises.LivingRoomToCreate livingRoom = TypeConverter.javaBean(ImportHouseOMSRequest.ApartmentHouse.ResidentialPremises.LivingRoomToCreate.class, r);
            premise.getLivingRoomToCreate().add(livingRoom);
        } else {
            ImportHouseOMSRequest.ApartmentHouse.ResidentialPremises.LivingRoomToUpdate livingRoom = TypeConverter.javaBean(ImportHouseOMSRequest.ApartmentHouse.ResidentialPremises.LivingRoomToUpdate.class, r);
            premise.getLivingRoomToUpdate().add(livingRoom);
        }
    }
    
    public static void add(ImportHouseRSORequest.LivingHouse house, Map<String, Object> r) {
        if (r.get("livingroomguid") == null) {
            ImportHouseRSORequest.LivingHouse.LivingRoomToCreate livingRoom = TypeConverter.javaBean(ImportHouseRSORequest.LivingHouse.LivingRoomToCreate.class, r);
            house.getLivingRoomToCreate().add(livingRoom);
        } else {
            ImportHouseRSORequest.LivingHouse.LivingRoomToUpdate livingRoom = TypeConverter.javaBean(ImportHouseRSORequest.LivingHouse.LivingRoomToUpdate.class, r);
            house.getLivingRoomToUpdate().add(livingRoom);
        }
    }
    
    public static void add(ImportHouseRSORequest.LivingHouse.Blocks block, Map<String, Object> r) {
        if (r.get("livingroomguid") == null) {
            ImportHouseRSORequest.LivingHouse.Blocks.LivingRoomToCreate livingRoom = TypeConverter.javaBean(ImportHouseRSORequest.LivingHouse.Blocks.LivingRoomToCreate.class, r);
            block.getLivingRoomToCreate().add(livingRoom);
        } else {
            ImportHouseRSORequest.LivingHouse.Blocks.LivingRoomToUpdate livingRoom = TypeConverter.javaBean(ImportHouseRSORequest.LivingHouse.Blocks.LivingRoomToUpdate.class, r);
            block.getLivingRoomToUpdate().add(livingRoom);
        }
    }
    
    public static void add(ImportHouseRSORequest.ApartmentHouse.ResidentialPremises premise, Map<String, Object> r) {
        if (r.get("livingroomguid") == null) {
            ImportHouseRSORequest.ApartmentHouse.ResidentialPremises.LivingRoomToCreate livingRoom = TypeConverter.javaBean(ImportHouseRSORequest.ApartmentHouse.ResidentialPremises.LivingRoomToCreate.class, r);
            premise.getLivingRoomToCreate().add(livingRoom);
        } else {
            ImportHouseRSORequest.ApartmentHouse.ResidentialPremises.LivingRoomToUpdate livingRoom = TypeConverter.javaBean(ImportHouseRSORequest.ApartmentHouse.ResidentialPremises.LivingRoomToUpdate.class, r);
            premise.getLivingRoomToUpdate().add(livingRoom);
        }
    }
    
    public static void add(ImportHouseESPRequest.ApartmentHouse.ResidentialPremises premise, Map<String, Object> r) {
        if (r.get("livingroomguid") == null) {
            ImportHouseESPRequest.ApartmentHouse.ResidentialPremises.LivingRoomToCreate livingRoom = TypeConverter.javaBean(ImportHouseESPRequest.ApartmentHouse.ResidentialPremises.LivingRoomToCreate.class, r);
            premise.getLivingRoomToCreate().add(livingRoom);
        } else {
            ImportHouseESPRequest.ApartmentHouse.ResidentialPremises.LivingRoomToUpdate livingRoom = TypeConverter.javaBean(ImportHouseESPRequest.ApartmentHouse.ResidentialPremises.LivingRoomToUpdate.class, r);
            premise.getLivingRoomToUpdate().add(livingRoom);
        }
    }
}