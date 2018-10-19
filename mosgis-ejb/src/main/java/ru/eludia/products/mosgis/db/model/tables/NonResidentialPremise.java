package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseESPRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseOMSRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseUORequest;

public class NonResidentialPremise extends Passport {
    
    public NonResidentialPremise () {
        
        super  ("tb_premises_nrs", "Нежилые помещения");
        
        pk     ("uuid",               Type.UUID,   NEW_UUID,           "Ключ");
        col    ("terminationdate",    Type.DATE,           null,       "Дата прекращения существования объекта");
        col    ("is_deleted",         Type.BOOLEAN,        Bool.FALSE, "1, если запись удалена; иначе 0");
        col    ("code_vc_nsi_330",    Type.STRING,  20,    null,       "Причина аннулирования");
        col    ("annulmentinfo",      Type.STRING,         null,       "Причина аннулирования. Дополнительная информация");

        col    ("annulmentreason",    Type.STRING,         new Virt ("''||\"CODE_VC_NSI_330\""),  "Причина аннулирования");
        col    ("is_annuled",         Type.BOOLEAN,        new Virt ("DECODE(\"CODE_VC_NSI_330\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");
        
        ref    ("uuid_house",         House.class,                     "Дом");
        
        col    ("premisesnum",        Type.STRING, 255,    null,       "Номер помещения");
        col    ("cadastralnumber",    Type.STRING,         null,       "Кадастровый номер");
        col    ("totalarea",          Type.NUMERIC, 25, 4, null,       "Общая площадь жилого помещения");
        col    ("iscommonproperty",   Type.BOOLEAN,        Bool.FALSE, "1, если помещение составляет общее имущество в многоквартирном доме; иначе 0");
        col    ("floor",              Type.STRING,         null,       "Этаж");
        
        ref    ("fiaschildhouseguid",    VocBuilding.class, null,      "ГУИД дочернего дома по ФИАС, к которому относится подъезд для группирующих домов");
        col    ("gis_unique_number",     Type.STRING,       null,      "Уникальный номер");
        col    ("gis_modification_date", Type.TIMESTAMP,    null,      "Дата модификации данных в ГИС ЖКХ");
        col    ("informationconfirmed",  Type.BOOLEAN,      Bool.TRUE, "Информация подтверждена поставщиком");
        col    ("premisesguid",          Type.UUID,         null,      "Идентификатор в ГИС ЖКХ");

        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "
            + "IF :NEW.premisesnum IS NULL        THEN raise_application_error (-20000, '#premisesnum#: Необходимо указать номер помещения.'); END IF; "
            + "IF  NVL (:NEW.totalarea, 0) <= 0 AND :OLD.totalarea > 0  THEN raise_application_error (-20000, '#totalarea#: Необходимо указать размер общей плошади.'); END IF; "
        + "END;");
        
    }
        
    @Override
    public void addNsiFields (DB db) throws SQLException {

        boolean isVirgin = refTables.isEmpty ();
        
            db.forEach (model.select (VocPassportFields.class, "*").where ("is_for_premise_nrs", 1).and ("id_type IS NOT NULL"), rs -> {

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
        if (r.get("premisesguid") == null) {
            ImportHouseUORequest.ApartmentHouse.NonResidentialPremiseToCreate premise = TypeConverter.javaBean(ImportHouseUORequest.ApartmentHouse.NonResidentialPremiseToCreate.class, r);
            house.getNonResidentialPremiseToCreate().add(premise);
        } else {
            ImportHouseUORequest.ApartmentHouse.NonResidentialPremiseToUpdate premise = TypeConverter.javaBean(ImportHouseUORequest.ApartmentHouse.NonResidentialPremiseToUpdate.class, r);
            house.getNonResidentialPremiseToUpdate().add(premise);
        }
    }
    
    public static void add(ImportHouseOMSRequest.ApartmentHouse house, Map<String, Object> r) {
        if (r.get("premisesguid") == null) {
            ImportHouseOMSRequest.ApartmentHouse.NonResidentialPremiseToCreate premise = TypeConverter.javaBean(ImportHouseOMSRequest.ApartmentHouse.NonResidentialPremiseToCreate.class, r);
            house.getNonResidentialPremiseToCreate().add(premise);
        } else {
            ImportHouseOMSRequest.ApartmentHouse.NonResidentialPremiseToUpdate premise = TypeConverter.javaBean(ImportHouseOMSRequest.ApartmentHouse.NonResidentialPremiseToUpdate.class, r);
            house.getNonResidentialPremiseToUpdate().add(premise);
        }
    }
    
    public static void add(ImportHouseRSORequest.ApartmentHouse house, Map<String, Object> r) {
        if (r.get("premisesguid") == null) {
            ImportHouseRSORequest.ApartmentHouse.NonResidentialPremiseToCreate premise = TypeConverter.javaBean(ImportHouseRSORequest.ApartmentHouse.NonResidentialPremiseToCreate.class, r);
            house.getNonResidentialPremiseToCreate().add(premise);
        } else {
            ImportHouseRSORequest.ApartmentHouse.NonResidentialPremiseToUpdate premise = TypeConverter.javaBean(ImportHouseRSORequest.ApartmentHouse.NonResidentialPremiseToUpdate.class, r);
            house.getNonResidentialPremiseToUpdate().add(premise);
        }
    }
    
    public static void add(ImportHouseESPRequest.ApartmentHouse house, Map<String, Object> r) {
        if (r.get("premisesguid") == null) {
            ImportHouseESPRequest.ApartmentHouse.NonResidentialPremiseToCreate premise = TypeConverter.javaBean(ImportHouseESPRequest.ApartmentHouse.NonResidentialPremiseToCreate.class, r);
            house.getNonResidentialPremiseToCreate().add(premise);
        } else {
            ImportHouseESPRequest.ApartmentHouse.NonResidentialPremiseToUpdate premise = TypeConverter.javaBean(ImportHouseESPRequest.ApartmentHouse.NonResidentialPremiseToUpdate.class, r);
            house.getNonResidentialPremiseToUpdate().add(premise);
        }
    }
    
}