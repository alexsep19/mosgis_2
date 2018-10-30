package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.util.SyncMap;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;
import ru.gosuslugi.dom.schema.integration.house_management.ContractServiceType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChResultType;
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

        col    ("is_additional",           Type.BOOLEAN,            new Virt    ("DECODE(RAWTOHEX(\"UUID_ADD_SERVICE\"),NULL,0,1)"),  "1, для дополнительной услуги, 0 для коммунальной");

        col    ("startdate",               Type.DATE,                           "Дата начала предоставления услуги");
        col    ("enddate",                 Type.DATE,                           "Дата окончания предоставления услуги");
        
        fk     ("id_log",                  ContractObjectServiceLog.class,      null, "Последнее событие редактирования");        

        trigger ("BEFORE INSERT", ""
                
        + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
        + "BEGIN "
                
            + "IF :NEW.is_deleted = 0 THEN "
            + " FOR i IN ("
                + "SELECT "
                + " o.startdate "
                + " , o.enddate "
                + "FROM "
                + " tb_contract_services o "
                + "WHERE o.is_deleted = 0"
                + " AND o.uuid_contract_object         =     :NEW.uuid_contract_object "
                + " AND o.uuid                        <>    :NEW.uuid "
                + " AND NVL(o.code_vc_nsi_3, ' ')     = NVL(:NEW.code_vc_nsi_3, ' ') "
                + " AND NVL(o.uuid_add_service, '00') = NVL(:NEW.uuid_add_service, '00') "
                + " AND o.enddate   >= :NEW.startdate "
                + " AND o.startdate <= :NEW.enddate "
                + ") LOOP"
            + " raise_application_error (-20000, "
                + "'В договоре по указанной услуге в рамках объекта управления уже есть запись с пересекающимся периодом действия: с ' "
                + "|| TO_CHAR (i.startdate, 'DD.MM.YYYY')"
                + "||' по '"
                + "|| TO_CHAR (i.enddate, 'DD.MM.YYYY')"
                + "|| '. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "                

            + "SELECT uuid_contract INTO :NEW.uuid_contract FROM tb_contract_objects WHERE uuid = :NEW.uuid_contract_object; "
                
        + "END;");
        
        trigger ("BEFORE INSERT OR UPDATE", " BEGIN "
                
            + "IF :NEW.startdate > :NEW.enddate THEN "
            + " raise_application_error (-20000, '#enddate#: Дата начала предоставления услуги должна быть раньше даты окончания');"
            + "END IF; "
                                
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
        
    abstract class Sync<T> extends SyncMap<T> {
        
        UUID uuid_contract;
        ContractFile.Sync contractFiles;
        
        public Sync (DB db, UUID uuid_contract, ContractFile.Sync contractFiles) {
            super (db);
            this.uuid_contract = uuid_contract;
            this.contractFiles = contractFiles;
            commonPart.put ("uuid_contract", uuid_contract);
            commonPart.put ("is_deleted", 0);
        }                
        
        void setFile (Map<String, Object> h, AttachmentType agreement) {
            h.put ("uuid_contract_agreement", contractFiles.getPk (agreement));
        }
        
        @Override
        public void processDeleted (List<Map<String, Object>> deleted) throws SQLException {
            
            deleted.forEach ((h) -> {
                Object u = h.get ("uuid");
                h.clear ();
                h.put ("uuid", u);
                h.put ("is_deleted", 1);
            });
            
            db.update (getTable (), deleted);

        }
        
        @Override
        public Table getTable () {
            return ContractObjectService.this;
        }
        
        void setDateFields (Map<String, Object> h, ContractServiceType cs) {
            h.put ("startdate", cs.getStartDate ());
            h.put ("enddate", cs.getEndDate ());
        }
        
    }    

    private final static String [] keyFieldsH = {"uuid_contract_object", "code_vc_nsi_3"};
    
    public class SyncH extends Sync<ExportCAChResultType.Contract.ContractObject.HouseService> {
        
        public SyncH (DB db, UUID uuid_contract, ContractFile.Sync contractFiles) {
            super (db, uuid_contract, contractFiles);
            commonPart.put ("is_additional", 0);            
        }                

        @Override
        public String[] getKeyFields () {
            return keyFieldsH;
        }

        @Override
        public void setFields (Map<String, Object> h, ExportCAChResultType.Contract.ContractObject.HouseService co) {
            setDateFields (h, co);
            h.put ("code_vc_nsi_3", co.getServiceType ().getCode ());
            setFile (h, co.getBaseService ().getAgreement ());            
        }
        
    }
    
    private final static String [] keyFieldsA = {"uuid_contract_object", "uuid_add_service"};
    
    public class SyncA extends Sync<ExportCAChResultType.Contract.ContractObject.AddService> {
        
        AdditionalService.Sync adds;

        public SyncA (DB db, UUID uuid_contract, ContractFile.Sync contractFiles, AdditionalService.Sync adds) throws SQLException {
            super (db, uuid_contract, contractFiles);
            this.adds = adds;            
            commonPart.put ("is_additional", 1);                                    
        }                

        @Override
        public String[] getKeyFields () {
            return keyFieldsA;
        }

        @Override
        public void setFields (Map<String, Object> h, ExportCAChResultType.Contract.ContractObject.AddService co) {
            setDateFields (h, co);
            h.put ("uuid_add_service", adds.getPk (co.getServiceType ()));
            setFile (h, co.getBaseService ().getAgreement ());
        }

    }    

}