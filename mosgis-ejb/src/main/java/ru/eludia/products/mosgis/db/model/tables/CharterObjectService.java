package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.gosuslugi.dom.schema.integration.house_management.ImportCharterRequest;

public class CharterObjectService extends Table {

    public CharterObjectService () {

        super  ("tb_charter_services", "Услуги по уставу");

        pk     ("uuid",                    Type.UUID,               NEW_UUID,   "Ключ");
        col    ("is_deleted",              Type.BOOLEAN,            Bool.FALSE, "1, если запись удалена; иначе 0");

        ref    ("uuid_charter_object",     CharterObject.class,                 "Ссылка на объект устава");
        fk     ("uuid_charter",            Charter.class,                       "Ссылка на устав");
        ref    ("uuid_charter_file",       CharterFile.class,    null,          "Ссылка на протокол");
                
        col    ("code_vc_nsi_3",           Type.STRING,  20,        null,       "Коммунальная услуга");
        ref    ("uuid_add_service",        AdditionalService.class, null,       "Дополнительная услуга");

        col    ("is_additional",           Type.BOOLEAN,            new Virt    ("DECODE(RAWTOHEX(\"UUID_ADD_SERVICE\"),NULL,0,1)"),  "1, для дополнительной услуги, 0 для коммунальной");

        col    ("startdate",               Type.DATE,                           "Дата начала предоставления услуги");
        col    ("enddate",                 Type.DATE,               null,       "Дата окончания предоставления услуги");
        
        fk     ("id_log",                  CharterObjectServiceLog.class,      null, "Последнее событие редактирования");        

        trigger ("BEFORE INSERT", "BEGIN "
            + "SELECT uuid_charter INTO :NEW.uuid_charter FROM tb_charter_objects WHERE uuid = :NEW.uuid_charter_object; "
        + "END;");
        
        trigger ("BEFORE INSERT OR UPDATE", ""
                
            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "
                
            + "IF :NEW.startdate > :NEW.enddate THEN "
            + " raise_application_error (-20000, '#enddate#: Дата начала предоставления услуги должна быть раньше даты окончания');"
            + "END IF; "

            + "IF :NEW.is_deleted = 0 THEN "
            + " FOR i IN ("
                + "SELECT "
                + " o.startdate "
                + " , o.enddate "
                + "FROM "
                + " tb_charter_services o "
                + "WHERE o.is_deleted = 0"
                + " AND o.uuid_charter_object         =     :NEW.uuid_charter_object "
                + " AND o.uuid                        <>    :NEW.uuid "
                + " AND NVL(o.code_vc_nsi_3, ' ')     = NVL(:NEW.code_vc_nsi_3, ' ') "
                + " AND NVL(o.uuid_add_service, '00') = NVL(:NEW.uuid_add_service, '00') "
                + " AND o.enddate   >= :NEW.startdate "
                + " AND o.startdate <= :NEW.enddate "
                + ") LOOP"
            + " raise_application_error (-20000, "
                + "'В уставе по указанной услуге в рамках объекта управления уже есть запись с пересекающимся периодом действия: с ' "
                + "|| TO_CHAR (i.startdate, 'DD.MM.YYYY')"
                + "||' по '"
                + "|| TO_CHAR (i.enddate, 'DD.MM.YYYY')"
                + "|| '. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "                

        + "END;");

    }
    
    static void add (ImportCharterRequest.PlacingCharter.ContractObject co, Map<String, Object> r) {
        switch (r.get ("is_additional").toString ()) {
            case "0": addHouse (co, r); break;
            case "1": addAdd   (co, r); break;
        }
    }
    
    private static void addHouse (ImportCharterRequest.PlacingCharter.ContractObject co, Map<String, Object> r) {
        ImportCharterRequest.PlacingCharter.ContractObject.HouseService hs = (ImportCharterRequest.PlacingCharter.ContractObject.HouseService) DB.to.javaBean (ImportCharterRequest.PlacingCharter.ContractObject.HouseService.class, r);
        hs.setServiceType (NsiTable.toDom ((String) r.get ("vc_nsi_3.code"), (UUID) r.get ("vc_nsi_3.guid")));
        hs.setBaseServiceCharter (CharterFile.getBaseServiceType (r));
        co.getHouseService ().add (hs);
    }
    
    private static void addAdd (ImportCharterRequest.PlacingCharter.ContractObject co, Map<String, Object> r) {
        ImportCharterRequest.PlacingCharter.ContractObject.AddService as = (ImportCharterRequest.PlacingCharter.ContractObject.AddService) DB.to.javaBean (ImportCharterRequest.PlacingCharter.ContractObject.AddService.class, r);
        as.setServiceType (NsiTable.toDom ((String) r.get ("add_service.uniquenumber"), (UUID) r.get ("add_service.elementguid")));
        as.setBaseServiceCharter (CharterFile.getBaseServiceType (r));
        co.getAddService ().add (as);
    }    
    
    static void add (ImportCharterRequest.EditCharter.ContractObject.Add co, Map<String, Object> r) {
        switch (r.get ("is_additional").toString ()) {
            case "0": addHouse (co, r); break;
            case "1": addAdd   (co, r); break;
        }
    }
    
    private static void addHouse (ImportCharterRequest.EditCharter.ContractObject.Add co, Map<String, Object> r) {
        ImportCharterRequest.EditCharter.ContractObject.Add.HouseService hs = (ImportCharterRequest.EditCharter.ContractObject.Add.HouseService) DB.to.javaBean (ImportCharterRequest.EditCharter.ContractObject.Add.HouseService.class, r);
        hs.setServiceType (NsiTable.toDom ((String) r.get ("vc_nsi_3.code"), (UUID) r.get ("vc_nsi_3.guid")));
        hs.setBaseServiceCharter (CharterFile.getBaseServiceType (r));
        co.getHouseService ().add (hs);
    }

    private static void addAdd (ImportCharterRequest.EditCharter.ContractObject.Add co, Map<String, Object> r) {
        ImportCharterRequest.EditCharter.ContractObject.Add.AddService as = (ImportCharterRequest.EditCharter.ContractObject.Add.AddService) DB.to.javaBean (ImportCharterRequest.EditCharter.ContractObject.Add.AddService.class, r);
        as.setServiceType (NsiTable.toDom ((String) r.get ("add_service.uniquenumber"), (UUID) r.get ("add_service.elementguid")));
        as.setBaseServiceCharter (CharterFile.getBaseServiceType (r));
        co.getAddService ().add (as);
    }
    
    static void add (ImportCharterRequest.EditCharter.ContractObject.Edit co, Map<String, Object> r) {
        switch (r.get ("is_additional").toString ()) {
            case "0": addHouse (co, r); break;
            case "1": addAdd   (co, r); break;
        }
    }
    
    private static void addHouse (ImportCharterRequest.EditCharter.ContractObject.Edit co, Map<String, Object> r) {
        ImportCharterRequest.EditCharter.ContractObject.Edit.HouseService hs = (ImportCharterRequest.EditCharter.ContractObject.Edit.HouseService) DB.to.javaBean (ImportCharterRequest.EditCharter.ContractObject.Edit.HouseService.class, r);
        hs.setServiceType (NsiTable.toDom ((String) r.get ("vc_nsi_3.code"), (UUID) r.get ("vc_nsi_3.guid")));
        hs.setBaseServiceCharter (CharterFile.getBaseServiceType (r));
        co.getHouseService ().add (hs);
    }

    private static void addAdd (ImportCharterRequest.EditCharter.ContractObject.Edit co, Map<String, Object> r) {
        ImportCharterRequest.EditCharter.ContractObject.Edit.AddService as = (ImportCharterRequest.EditCharter.ContractObject.Edit.AddService) DB.to.javaBean (ImportCharterRequest.EditCharter.ContractObject.Edit.AddService.class, r);
        as.setServiceType (NsiTable.toDom ((String) r.get ("add_service.uniquenumber"), (UUID) r.get ("add_service.elementguid")));
        as.setBaseServiceCharter (CharterFile.getBaseServiceType (r));
        co.getAddService ().add (as);
    }
    
}