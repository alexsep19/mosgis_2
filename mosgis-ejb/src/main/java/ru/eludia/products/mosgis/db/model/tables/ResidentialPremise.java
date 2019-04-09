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
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocHouseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseESPRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseOMSRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseUORequest;

public class ResidentialPremise extends Passport {
    
    public ResidentialPremise () {
        
        super  ("tb_premises_res", "Жилые помещения");
        
        pk     ("uuid",               Type.UUID,   NEW_UUID,           "Ключ");
	ref    ("uuid_org",           VocOrganization.class, null, "Организация, которая завела данное помещение в БД");
	fk     ("uuid_xl",            InXlFile.class,      null,       "Источник импорта");
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
        
        ref    ("fiaschildhouseguid",    VocBuilding.class, null,       "ГУИД дочернего дома по ФИАС, к которому относится подъезд для группирующих домов");
        col    ("gis_unique_number",     Type.STRING,       null,       "Уникальный номер");
        col    ("gis_modification_date", Type.TIMESTAMP,    null,       "Дата модификации данных в ГИС ЖКХ");
        col    ("informationconfirmed",  Type.BOOLEAN,      Bool.TRUE,  "Информация подтверждена поставщиком");
        col    ("premisesguid",          Type.UUID,         null,       "Идентификатор в ГИС ЖКХ");
        col    ("is_annuled_in_gis",     Type.BOOLEAN,      Bool.FALSE, "1, если запись аннулирована в ГИС ЖКХ; иначе 0");
        
        fk     ("id_status", VocHouseStatus.class, new Virt("DECODE(\"PREMISESGUID\",NULL," + VocHouseStatus.i.MISSING.getId() + "," + VocHouseStatus.i.PUBLISHED.getId() + ")"), "Статус размещения в ГИС ЖКХ");
        
        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "
                
            + "IF :NEW.totalarea < :NEW.grossarea THEN raise_application_error (-20000, '#grossarea#: Жилая площадь не может превышать общую.'); END IF; "
                
            + "IF INSERTING THEN "
                
                + "FOR i IN (SELECT premisesnum, is_annuled_in_gis FROM tb_premises_res WHERE uuid_house = :NEW.uuid_house AND premisesnum = :NEW.premisesnum AND is_deleted = 0) LOOP "
                    + "IF (i.is_annuled_in_gis <> 1) THEN "
                        + "raise_application_error (-20000, 'Аннулирование записи жилого помещения с номером ' || i.premisesnum || ' не подтверждено в ГИС. Операция отменена.'); "
                    + "END IF; "
                + "END LOOP; "
                
            + "END IF; "
                
            + "IF UPDATING THEN "
                + "IF :NEW.is_deleted = 1 AND :OLD.is_deleted = 0 AND :OLD.id_status = " + VocHouseStatus.i.PUBLISHED.getId () + " THEN raise_application_error (-20000, 'Запись размещена в ГИС. Операция прервана.'); END IF; "
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
        
            db.forEach (model.select (VocPassportFields.class, "id", "label", "id_type", "is_multiple").where ("is_for_premise_res", 1).and ("id_type IS NOT NULL"), rs -> {

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
    
    public static void add(ImportHouseUORequest.ApartmentHouse house, Map<String, Object> r) {
        
        ImportHouseUORequest.ApartmentHouse.ResidentialPremises premise = new ImportHouseUORequest.ApartmentHouse.ResidentialPremises();
        house.getResidentialPremises().add(premise);
        
        if (r.get("premisesguid") == null) {
            ImportHouseUORequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToCreate premisesToCreate = 
                    TypeConverter.javaBean(ImportHouseUORequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToCreate.class, r);
            premise.setResidentialPremisesToCreate(premisesToCreate);
        } else {
            ImportHouseUORequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToUpdate  premisesToUpdate = 
                    TypeConverter.javaBean(ImportHouseUORequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToUpdate.class, r);
            premise.setResidentialPremisesToUpdate(premisesToUpdate);
        }
        
        ((Collection<Map<String, Object>>) r.get("livingrooms")).forEach((room) -> LivingRoom.add(premise, room));
    }
    
    public static void add(ImportHouseOMSRequest.ApartmentHouse house, Map<String, Object> r) {
        
        ImportHouseOMSRequest.ApartmentHouse.ResidentialPremises premise = new ImportHouseOMSRequest.ApartmentHouse.ResidentialPremises();
        house.getResidentialPremises().add(premise);
        
        if (r.get("premisesguid") == null) {
            ImportHouseOMSRequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToCreate premisesToCreate = 
                    TypeConverter.javaBean(ImportHouseOMSRequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToCreate.class, r);
            premise.setResidentialPremisesToCreate(premisesToCreate);
        } else {
            ImportHouseOMSRequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToUpdate  premisesToUpdate = 
                    TypeConverter.javaBean(ImportHouseOMSRequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToUpdate.class, r);
            premise.setResidentialPremisesToUpdate(premisesToUpdate);
        }
        
        ((Collection<Map<String, Object>>) r.get("livingrooms")).forEach((room) -> LivingRoom.add(premise, room));
    }
    
    public static void add(ImportHouseRSORequest.ApartmentHouse house, Map<String, Object> r) {
        
        ImportHouseRSORequest.ApartmentHouse.ResidentialPremises premise = new ImportHouseRSORequest.ApartmentHouse.ResidentialPremises();
        house.getResidentialPremises().add(premise);
        
        if (r.get("premisesguid") == null) {
            ImportHouseRSORequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToCreate premisesToCreate = 
                    TypeConverter.javaBean(ImportHouseRSORequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToCreate.class, r);
            premise.setResidentialPremisesToCreate(premisesToCreate);
        } else {
            ImportHouseRSORequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToUpdate  premisesToUpdate = 
                    TypeConverter.javaBean(ImportHouseRSORequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToUpdate.class, r);
            premise.setResidentialPremisesToUpdate(premisesToUpdate);
        }
        
        ((Collection<Map<String, Object>>) r.get("livingrooms")).forEach((room) -> LivingRoom.add(premise, room));
    }
    
    public static void add(ImportHouseESPRequest.ApartmentHouse house, Map<String, Object> r) {
        
        ImportHouseESPRequest.ApartmentHouse.ResidentialPremises premise = new ImportHouseESPRequest.ApartmentHouse.ResidentialPremises();
        house.getResidentialPremises().add(premise);
        
        if (r.get("premisesguid") == null) {
            ImportHouseESPRequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToCreate premisesToCreate = 
                    TypeConverter.javaBean(ImportHouseESPRequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToCreate.class, r);
            premise.setResidentialPremisesToCreate(premisesToCreate);
        } else {
            ImportHouseESPRequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToUpdate  premisesToUpdate = 
                    TypeConverter.javaBean(ImportHouseESPRequest.ApartmentHouse.ResidentialPremises.ResidentialPremisesToUpdate.class, r);
            premise.setResidentialPremisesToUpdate(premisesToUpdate);
        }
        
        ((Collection<Map<String, Object>>) r.get("livingrooms")).forEach((room) -> LivingRoom.add(premise, room));
    }
    
}