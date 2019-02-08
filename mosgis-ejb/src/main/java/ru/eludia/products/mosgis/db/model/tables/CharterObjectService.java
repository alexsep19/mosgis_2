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
import ru.gosuslugi.dom.schema.integration.house_management.ImportCharterRequest;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

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
    
    abstract class Sync<T> extends SyncMap<T> {
        
        UUID uuid_charter;
        CharterFile.Sync files;
        
        public Sync (DB db, UUID uuid_charter, CharterFile.Sync files) {
            super (db);
            this.uuid_charter = uuid_charter;
            this.files = files;
            commonPart.put ("uuid_charter", uuid_charter);
//          commonPart.put ("is_deleted", 0);
        }                
        
        void setFile (Map<String, Object> h, AttachmentType agreement) {
            h.put ("uuid_charter_agreement", files.getPk (agreement));
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
            return CharterObjectService.this;
        }
        
        void setDateFields (Map<String, Object> h, ContractServiceType cs) {
            h.put ("startdate", cs.getStartDate ());
            h.put ("enddate", cs.getEndDate ());
        }
        
    }    

    private final static String [] keyFieldsH = {"uuid_charter_object", "code_vc_nsi_3"};
    
    public class SyncH extends Sync<ExportCAChResultType.Charter.ContractObject.HouseService> {
        
        public SyncH (DB db, UUID uuid_charter, CharterFile.Sync contractFiles) {
            super (db, uuid_charter, contractFiles);
            commonPart.put ("is_additional", 0);            
        }                

        @Override
        public String[] getKeyFields () {
            return keyFieldsH;
        }

        @Override
        public void setFields (Map<String, Object> h, ExportCAChResultType.Charter.ContractObject.HouseService co) {
            setDateFields (h, co);
            h.put ("code_vc_nsi_3", co.getServiceType ().getCode ());
            setFile (h, co.getBaseService ().getAgreement ());            
        }
        
    }
    
    private final static String [] keyFieldsA = {"uuid_charter_object", "uuid_add_service"};
    
    public class SyncA extends Sync<ExportCAChResultType.Charter.ContractObject.AddService> {
        
        AdditionalService.Sync adds;

        public SyncA (DB db, UUID uuid_charter, CharterFile.Sync contractFiles, AdditionalService.Sync adds) throws SQLException {
            super (db, uuid_charter, contractFiles);
            this.adds = adds;            
            commonPart.put ("is_additional", 1);                                    
        }                

        @Override
        public String[] getKeyFields () {
            return keyFieldsA;
        }

        @Override
        public void setFields (Map<String, Object> h, ExportCAChResultType.Charter.ContractObject.AddService co) {

            setDateFields (h, co);
            
            NsiRef st = co.getServiceType ();

            String code = st.getCode ();

            Map<String, Object> add = adds.get (code);

            if (add == null) throw new IllegalStateException ("Не найдена услуга с кодом '" + code + "' среди " + adds.keySet ());
            Object uuuuuuuuid = add.get ("uuid");
            
            h.put ("uuid_add_service", uuuuuuuuid);
            
            setFile (h, co.getBaseService ().getAgreement ());
            
        }

    }        
    
}