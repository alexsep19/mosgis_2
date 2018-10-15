package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import static ru.eludia.products.mosgis.db.model.voc.VocGisStatus.i.MUTATING;
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
        
        trigger ("BEFORE INSERT OR UPDATE", " BEGIN "
                
            + "IF :NEW.startdate > :NEW.enddate THEN "
            + " raise_application_error (-20000, '#enddate#: Дата начала предоставления услуги должна быть раньше даты окончания');"
            + "END IF; "
                
            + " UPDATE tb_charter_objects SET id_ctr_status = " + MUTATING.getId () + " WHERE uuid = :NEW.uuid_charter_object AND contractobjectversionguid IS NOT NULL; "
                
        + "END;");

    }
    
    static void add (ImportCharterRequest.PlacingCharter.ContractObject co, Map<String, Object> r) {
        switch (r.get ("is_additional").toString ()) {
            case "0": addHouse (co, r); break;
            case "1": addAdd   (co, r); break;
        }
    }
    
    private static void addHouse (ImportCharterRequest.PlacingCharter.ContractObject co, Map<String, Object> r) {
        ImportCharterRequest.PlacingCharter.ContractObject.HouseService hs = new ImportCharterRequest.PlacingCharter.ContractObject.HouseService ();
        hs.setServiceType (NsiTable.toDom ((String) r.get ("vc_nsi_3.code"), (UUID) r.get ("vc_nsi_3.guid")));
        hs.setBaseServiceCharter (CharterFile.getBaseServiceType (r));
        co.getHouseService ().add (hs);
    }
    
    private static void addAdd (ImportCharterRequest.PlacingCharter.ContractObject co, Map<String, Object> r) {
        ImportCharterRequest.PlacingCharter.ContractObject.AddService as = new ImportCharterRequest.PlacingCharter.ContractObject.AddService ();
        as.setServiceType (NsiTable.toDom ((String) r.get ("add_service.uniquenumber"), (UUID) r.get ("add_service.elementguid")));
        as.setBaseServiceCharter (CharterFile.getBaseServiceType (r));
        co.getAddService ().add (as);
    }    
    
}