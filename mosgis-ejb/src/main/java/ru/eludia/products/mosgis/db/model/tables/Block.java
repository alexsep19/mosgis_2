package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocHouseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseOMSRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseUORequest;

public class Block extends Passport {
    
    public enum c implements ColEnum {
        
        UUID        (Type.UUID,           NEW_UUID,   "Ключ"),
        IS_DELETED  (Type.BOOLEAN,        Bool.FALSE, "1, если запись удалена; иначе 0"),
        UUID_XL     (InXlFile.class,      null,       "Источник импорта"),
	UUID_ORG    (VocOrganization.class, null, "Организация, которая завела данное помещение в БД"),

        TERMINATIONDATE (Type.DATE,           null,       "Дата прекращения существования объекта"),
        CODE_VC_NSI_330 (Type.STRING,  20,    null,       "Причина аннулирования"),
        ANNULMENTINFO   (Type.STRING,         null,       "Причина аннулирования. Дополнительная информация"),
        
        ANNULMENTREASON (Type.STRING,         new Virt ("''||\"CODE_VC_NSI_330\""),  "Причина аннулирования"),
        IS_ANNULED      (Type.BOOLEAN,        new Virt ("DECODE(\"CODE_VC_NSI_330\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0"),

        IS_NRS          (Type.BOOLEAN,        Bool.FALSE, "1, если блок нежилой; иначе 0"),

        UUID_HOUSE      (House.class,                     "Дом"),
        BLOCKNUM        (Type.STRING, 255,    null,       "Номер блока"),

        CADASTRALNUMBER (Type.STRING,         null,       "Кадастровый номер"),
        CODE_VC_NSI_30  (Type.STRING,  20,    null,       "Характеристика помещения"),
        TOTALAREA       (Type.NUMERIC, 25, 4, null,       "Общая площадь помещения"),
        GROSSAREA       (Type.NUMERIC, 25, 4, null,       "Жилая площадь помещения"),
        
        F_20002               (Type.NUMERIC, 10, null, "Количество комнат (НСИ 14)"),
        F_20125               (Type.NUMERIC, 10, null, "Количество проживающих"),
        F_20003               (Type.NUMERIC, 10, null, "Назначение помещения, относящегося к общему долевому имуществу собственников помещений (НСИ 17)"),
        
        GIS_UNIQUE_NUMBER     (Type.STRING,      null,       "Уникальный номер"),
        GIS_MODIFICATION_DATE (Type.TIMESTAMP,   null,       "Дата модификации данных в ГИС ЖКХ"),
        INFORMATIONCONFIRMED  (Type.BOOLEAN,     Bool.TRUE,  "Информация подтверждена поставщиком"),
        BLOCKGUID             (Type.UUID,        null,       "Идентификатор в ГИС ЖКХ"),
        IS_ANNULED_IN_GIS     (Type.BOOLEAN,     Bool.FALSE, "1, если запись аннулирована в ГИС ЖКХ; иначе 0"),
        
        ID_STATUS             (VocHouseStatus.class, new Virt("DECODE(\"BLOCKGUID\",NULL," + VocHouseStatus.i.MISSING.getId() + "," + VocHouseStatus.i.PUBLISHED.getId() + ")"), "Статус размещения в ГИС ЖКХ")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        public boolean isToXlImport () {
            
            switch (this) {
                case UUID_XL:
                case BLOCKNUM:
                case IS_NRS:
                case CODE_VC_NSI_30:
                case TOTALAREA:
                case GROSSAREA:
                case CADASTRALNUMBER:
                case INFORMATIONCONFIRMED:
                case F_20002:
                case F_20125:
                case F_20003:
                    return true;
                default:
                    return false;
            }
            
        }
        
    }
    
    public Block () {
        
        super  ("tb_blocks", "Блоки (для жилого дома блокированной застройки)");
        
        cols   (c.class);
        
        pk     (c.UUID);
        
        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "
                
            + "IF :NEW.totalarea < :NEW.grossarea THEN raise_application_error (-20000, '#grossarea#: Жилая площадь не может превышать общую.'); END IF; "
                
            + "IF INSERTING THEN "
                
                + "FOR i IN (SELECT blocknum, is_annuled_in_gis FROM tb_blocks WHERE uuid_house = :NEW.uuid_house AND blocknum = :NEW.blocknum AND is_deleted = 0) LOOP "
                    + "IF (i.is_annuled_in_gis <> 1) THEN "
                        + "raise_application_error (-20000, 'Аннулирование записи блока с номером ' || i.blocknum || ' не пожтверждено в ГИС. Операция отменена.'); "
                    + "END IF; "
                + "END LOOP; "
                
            + "END IF; "
                
            + "IF UPDATING THEN "
                + "IF :NEW.is_deleted = 1 AND :OLD.is_deleted = 0 AND :OLD.id_status = " + VocHouseStatus.i.PUBLISHED.getId () + " THEN raise_application_error (-20000, 'Запись размещена в ГИС. Операция прервана.'); END IF; "
                + "IF :NEW.blocknum IS NULL         AND :OLD.blocknum IS NOT NULL THEN raise_application_error (-20000, '#blocknum#: Необходимо указать номер помещения.'); END IF; "
                + "IF :NEW.is_nrs=0 AND :NEW.code_vc_nsi_30 IS NULL   AND :OLD.code_vc_nsi_30 IS NOT NULL  THEN raise_application_error (-20000, '#code_vc_nsi_30#: Необходимо указать характеристику помещения.'); END IF; "
                + "IF  NVL (:NEW.totalarea, 0) <= 0 AND :OLD.totalarea > 0  THEN raise_application_error (-20000, '#totalarea#: Необходимо указать размер общей плошади.'); END IF; "
                + "IF :NEW.is_nrs=0 AND NVL (:NEW.grossarea, 0) <= 0 AND :OLD.grossarea > 0  THEN raise_application_error (-20000, '#grossarea#: Необходимо указать размер жилой плошади.'); END IF; "
//                + "IF :NEW.f_20002 IS NULL          AND :OLD.f_20002 IS NOT NULL THEN raise_application_error (-20000, '#f_20002#: Необходимо указать количество комнат.'); END IF; "
            + "END IF;"
                
        + "END;");

    }
        
    @Override
    public void addNsiFields (DB db) throws SQLException {

        boolean isVirgin = refTables.isEmpty ();
        
            db.forEach (model.select (VocPassportFields.class, "id", "label", "id_type", "is_multiple").where ("is_for_block", 1).and ("id_type IS NOT NULL"), rs -> {

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
        
        ImportHouseUORequest.LivingHouse.Blocks block = new ImportHouseUORequest.LivingHouse.Blocks();
        house.getBlocks().add(block);
        
        if (r.get("blockguid") == null) {
            ImportHouseUORequest.LivingHouse.Blocks.BlockToCreate blockToCreate = 
                    TypeConverter.javaBean(ImportHouseUORequest.LivingHouse.Blocks.BlockToCreate.class, r);
            block.setBlockToCreate(blockToCreate);
        } else {
            ImportHouseUORequest.LivingHouse.Blocks.BlockToUpdate blockToUpdate = 
                    TypeConverter.javaBean(ImportHouseUORequest.LivingHouse.Blocks.BlockToUpdate.class, r);
            block.setBlockToUpdate(blockToUpdate);
        }
        
        ((Collection<Map<String, Object>>) r.get("livingrooms")).forEach((room) -> LivingRoom.add(block, room));
    }
    
    public static void add(ImportHouseOMSRequest.LivingHouse house, Map<String, Object> r) {
        
        ImportHouseOMSRequest.LivingHouse.Blocks block = new ImportHouseOMSRequest.LivingHouse.Blocks();
        house.getBlocks().add(block);
        
        if (r.get("blockguid") == null) {
            ImportHouseOMSRequest.LivingHouse.Blocks.BlockToCreate blockToCreate = 
                    TypeConverter.javaBean(ImportHouseOMSRequest.LivingHouse.Blocks.BlockToCreate.class, r);
            block.setBlockToCreate(blockToCreate);
        } else {
            ImportHouseOMSRequest.LivingHouse.Blocks.BlockToUpdate blockToUpdate = 
                    TypeConverter.javaBean(ImportHouseOMSRequest.LivingHouse.Blocks.BlockToUpdate.class, r);
            block.setBlockToUpdate(blockToUpdate);
        }
        
        ((Collection<Map<String, Object>>) r.get("livingrooms")).forEach((room) -> LivingRoom.add(block, room));
    }
    
    public static void add(ImportHouseRSORequest.LivingHouse house, Map<String, Object> r) {
        
        ImportHouseRSORequest.LivingHouse.Blocks block = new ImportHouseRSORequest.LivingHouse.Blocks();
        house.getBlocks().add(block);
        
        if (r.get("blockguid") == null) {
            ImportHouseRSORequest.LivingHouse.Blocks.BlockToCreate blockToCreate = 
                    TypeConverter.javaBean(ImportHouseRSORequest.LivingHouse.Blocks.BlockToCreate.class, r);
            block.setBlockToCreate(blockToCreate);
        } else {
            ImportHouseRSORequest.LivingHouse.Blocks.BlockToUpdate blockToUpdate = 
                    TypeConverter.javaBean(ImportHouseRSORequest.LivingHouse.Blocks.BlockToUpdate.class, r);
            block.setBlockToUpdate(blockToUpdate);
        }
        
        ((Collection<Map<String, Object>>) r.get("livingrooms")).forEach((room) -> LivingRoom.add(block, room));
    }
    
}