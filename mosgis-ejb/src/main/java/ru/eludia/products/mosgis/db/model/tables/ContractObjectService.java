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
import static ru.eludia.products.mosgis.db.model.voc.VocGisStatus.i.MUTATING;
import ru.gosuslugi.dom.schema.integration.house_management.ImportContractRequest;

public class ContractObjectService extends Table {

    public ContractObjectService () {

        super  ("tb_contract_services", "Услуги по договору");

        pk     ("uuid",                    Type.UUID,               NEW_UUID,   "Ключ");
        col    ("is_deleted",              Type.BOOLEAN,            Bool.FALSE, "1, если запись удалена; иначе 0");

        ref    ("uuid_contract_object",    ContractObject.class,                "Ссылка на объект договора");
        fk     ("uuid_contract",           Contract.class,                      "Ссылка на договор");
        
        ref    ("uuid_contract_agreement", ContractFile.class,      null,       "Ссылка на дополнительное соглашение");
        
        col    ("code_vc_nsi_3",           Type.STRING,  20,        null,       "Коммунальная услуга");
        ref    ("uuid_add_service",        AdditionalService.class, null,       "Дополнительная услуга");

        col    ("is_additional",           Type.BOOLEAN,            new Virt    ("DECODE(\"UUID_ADD_SERVICE\",NULL,0,1)"),  "1, для дополнительной услуги, 0 для коммунальной");

        col    ("startdate",               Type.DATE,                           "Дата начала предоставления услуги");
        col    ("enddate",                 Type.DATE,                           "Дата окончания предоставления услуги");
        
        fk     ("id_log",                  ContractObjectServiceLog.class,      null, "Последнее событие редактирования");        

        trigger ("BEFORE INSERT", "BEGIN "
            + "SELECT uuid_contract INTO :NEW.uuid_contract FROM tb_contract_objects WHERE uuid = :NEW.uuid_contract_object; "
        + "END;");
        
        trigger ("BEFORE INSERT OR UPDATE", " BEGIN "
                
            + "IF :NEW.startdate > :NEW.enddate THEN "
            + " raise_application_error (-20000, '#enddate#: Дата начала предоставления услуги должна быть раньше даты окончания');"
            + "END IF; "
                
            + " UPDATE tb_contract_objects SET id_ctr_status = " + MUTATING.getId () + " WHERE uuid = :NEW.uuid_contract_object AND contractobjectversionguid IS NOT NULL; "
                
        + "END;");

    }
    
    public static void add (ImportContractRequest.Contract.PlacingContract.ContractObject co, Map<String, Object> r) {
        switch (r.get ("is_additional").toString ()) {
            case "0": addHouse (co, r); break;
            case "1": addAdd   (co, r); break;
        }
    }
    
    static void add (ImportContractRequest.Contract.EditContract.ContractObject.Add co, Map<String, Object> r) {
        switch (r.get ("is_additional").toString ()) {
            case "0": addHouse (co, r); break;
            case "1": addAdd   (co, r); break;
        }
    }
    
    static void add (ImportContractRequest.Contract.EditContract.ContractObject.Edit co, Map<String, Object> r) {
        switch (r.get ("is_additional").toString ()) {
            case "0": addHouse (co, r); break;
            case "1": addAdd   (co, r); break;
        }
    }    
    
    private static void addAdd (ImportContractRequest.Contract.EditContract.ContractObject.Edit co, Map<String, Object> r) {

        final ImportContractRequest.Contract.EditContract.ContractObject.Edit.AddService as = (ImportContractRequest.Contract.EditContract.ContractObject.Edit.AddService) DB.to.javaBean (ImportContractRequest.Contract.EditContract.ContractObject.Edit.AddService.class, r);
        
        as.setServiceType (NsiTable.toDom ((String) r.get ("add_service.uniquenumber"), (UUID) r.get ("add_service.elementguid")));
        as.setBaseService (ContractFile.getBaseServiceType (r));
                
        co.getAddService ().add (as);

    }
    
        
    private static void addAdd (ImportContractRequest.Contract.EditContract.ContractObject.Add co, Map<String, Object> r) {

        final ImportContractRequest.Contract.EditContract.ContractObject.Add.AddService as = (ImportContractRequest.Contract.EditContract.ContractObject.Add.AddService) DB.to.javaBean (ImportContractRequest.Contract.EditContract.ContractObject.Add.AddService.class, r);
        
        as.setServiceType (NsiTable.toDom ((String) r.get ("add_service.uniquenumber"), (UUID) r.get ("add_service.elementguid")));
        as.setBaseService (ContractFile.getBaseServiceType (r));
                
        co.getAddService ().add (as);

    }
    
    private static void addAdd (ImportContractRequest.Contract.PlacingContract.ContractObject co, Map<String, Object> r) {

        final ImportContractRequest.Contract.PlacingContract.ContractObject.AddService as = (ImportContractRequest.Contract.PlacingContract.ContractObject.AddService) DB.to.javaBean (ImportContractRequest.Contract.PlacingContract.ContractObject.AddService.class, r);
        
        as.setServiceType (NsiTable.toDom ((String) r.get ("add_service.uniquenumber"), (UUID) r.get ("add_service.elementguid")));
        as.setBaseService (ContractFile.getBaseServiceType (r));
                
        co.getAddService ().add (as);
        
    }
    
    private static void addHouse (ImportContractRequest.Contract.EditContract.ContractObject.Edit co, Map<String, Object> r) {
        
        final ImportContractRequest.Contract.EditContract.ContractObject.Edit.HouseService hs = (ImportContractRequest.Contract.EditContract.ContractObject.Edit.HouseService) DB.to.javaBean (ImportContractRequest.Contract.EditContract.ContractObject.Edit.HouseService.class, r);
        
        hs.setServiceType (NsiTable.toDom ((String) r.get ("vc_nsi_3.code"), (UUID) r.get ("vc_nsi_3.guid")));
        hs.setBaseService (ContractFile.getBaseServiceType (r));
        
        co.getHouseService ().add (hs);
        
    }    

    private static void addHouse (ImportContractRequest.Contract.EditContract.ContractObject.Add co, Map<String, Object> r) {
        
        final ImportContractRequest.Contract.EditContract.ContractObject.Add.HouseService hs = (ImportContractRequest.Contract.EditContract.ContractObject.Add.HouseService) DB.to.javaBean (ImportContractRequest.Contract.EditContract.ContractObject.Add.HouseService.class, r);
        
        hs.setServiceType (NsiTable.toDom ((String) r.get ("vc_nsi_3.code"), (UUID) r.get ("vc_nsi_3.guid")));
        hs.setBaseService (ContractFile.getBaseServiceType (r));
        
        co.getHouseService ().add (hs);
        
    }
    
    private static void addHouse (ImportContractRequest.Contract.PlacingContract.ContractObject co, Map<String, Object> r) {
        
        final ImportContractRequest.Contract.PlacingContract.ContractObject.HouseService hs = (ImportContractRequest.Contract.PlacingContract.ContractObject.HouseService) DB.to.javaBean (ImportContractRequest.Contract.PlacingContract.ContractObject.HouseService.class, r);
        
        hs.setServiceType (NsiTable.toDom ((String) r.get ("vc_nsi_3.code"), (UUID) r.get ("vc_nsi_3.guid")));
        hs.setBaseService (ContractFile.getBaseServiceType (r));
        
        co.getHouseService ().add (hs);

    }

}