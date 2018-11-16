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
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocCharterObjectReason;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChResultType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportCharterRequest;

public class CharterObject extends Table {

    public CharterObject () {
        
        super  ("tb_charter_objects", "Объекты уставов");
        
        pk     ("uuid",                    Type.UUID,             NEW_UUID,     "Ключ");
        col    ("is_deleted",              Type.BOOLEAN,          Bool.FALSE,   "1, если запись удалена; иначе 0");
        col    ("is_from_gis",             Type.BOOLEAN,          Bool.FALSE,   "1, если запись импортирована из WS ГИС; иначе (если создана оператором системы) 0");
        
        ref    ("uuid_charter",           Charter.class,                      "Ссылка на договор");
        
        fk     ("fiashouseguid",           VocBuilding.class,                   "Глобальный уникальный идентификатор дома по ФИАС");
        
        fk     ("id_reason",               VocCharterObjectReason.class, new Num (VocCharterObjectReason.i.CHARTER.getId ()), "Основание");
        ref    ("uuid_charter_file",       CharterFile.class,     null,          "Ссылка на протокол");
        col    ("ismanagedbycontract",     Type.BOOLEAN,          Bool.FALSE,   "Управление многоквартирным домом осуществляется управляющей организацией по договору управления");

        col    ("startdate",               Type.DATE,             null,         "Дата начала предоставления услуг");
        col    ("enddate",                 Type.DATE,             null,         "Дата окончания предоставления услуг");        
       
        col    ("annulmentinfo",           Type.STRING,           null,       "Причина аннулирования.");
        col    ("is_annuled",              Type.BOOLEAN,          new Virt ("DECODE(\"ANNULMENTINFO\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");
        
        fk     ("id_ctr_status",           VocGisStatus.class,                  new Num (VocGisStatus.i.PROJECT.getId ()), "Статус объекта договора с точки зрения mosgis");
        fk     ("id_ctr_status_gis",       VocGisStatus.class,                  new Num (VocGisStatus.i.PROJECT.getId ()), "Статус объекта договора с точки зрения ГИС ЖКХ");

        col    ("contractobjectversionguid",     Type.UUID,       null,          "UUID последней версии данного объекта в ГИС ЖКХ");
        
        fk     ("id_log",                  CharterObjectLog.class,  null,      "Последнее событие редактирования");
 
        trigger ("BEFORE INSERT OR UPDATE", ""
                
            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "
                
            + "IF :NEW.is_deleted = 0 AND :NEW.is_annuled = 0 AND :NEW.ismanagedbycontract = 0 THEN "
            + " FOR i IN ("
                + "SELECT "
                + " o.startdate"
                + " , o.enddate"
                + " , c.docnum"
                + " , c.signingdate"
                + " , org.label "
                + "FROM "
                + " tb_contract_objects o "
                + " INNER JOIN tb_contracts c ON o.uuid_contract = c.uuid"
                + " INNER JOIN vc_orgs org    ON c.uuid_org      = org.uuid "
                + "WHERE o.is_deleted = 0"
                + " AND o.is_annuled = 0"
                + " AND o.fiashouseguid = :NEW.fiashouseguid "
                +                          " AND o.enddate   >= :NEW.startdate "
                + " AND (:NEW.enddate IS NULL OR o.startdate <= :NEW.enddate )"
                + ") LOOP"
            + " raise_application_error (-20000, "
                + "'Этот адрес обслуживается с ' "
                + "|| TO_CHAR (i.startdate, 'DD.MM.YYYY')"
                + "||' по '"
                + "|| TO_CHAR (i.enddate, 'DD.MM.YYYY')"
                + "||' по договору управления от '"
                + "|| TO_CHAR (i.signingdate, 'DD.MM.YYYY')"
                + "||' №'"
                + "|| i.docnum"
                + "||' с '"
                + "|| i.label"
                + "|| '. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "

                    
            + "IF :NEW.is_deleted = 0 AND :NEW.is_annuled = 0 AND :NEW.is_from_gis = 0 THEN "
            + " FOR i IN ("
                + "SELECT "
                + " o.startdate"
                + " , o.enddate"
                + " , org.label "
                + "FROM "
                + " tb_charter_objects o "
                + " INNER JOIN tb_charters c ON o.uuid_charter = c.uuid"
                + " INNER JOIN vc_orgs org    ON c.uuid_org      = org.uuid "
                + "WHERE o.is_deleted = 0"
                + " AND o.is_annuled = 0"
                + " AND o.fiashouseguid = :NEW.fiashouseguid "
                + " AND (   o.enddate IS NULL OR o.enddate >= :NEW.startdate) "
                + " AND (:NEW.enddate IS NULL OR o.startdate <= :NEW.enddate )"
                + " AND o.uuid <> NVL(:NEW.uuid, '00') "
                + ") LOOP"
            + " raise_application_error (-20000, "
                + "'Этот адрес обслуживается с ' "
                + "|| TO_CHAR (i.startdate, 'DD.MM.YYYY')"
                + "|| CASE WHEN i.enddate IS NULL THEN NULL ELSE ' по '"
                + "|| TO_CHAR (i.enddate, 'DD.MM.YYYY') END "
                + "||' согласно уставу '"
                + "|| i.label"
                + "|| '. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "
                    
        + "END;");
    }
    
    public static void add (ImportCharterRequest.PlacingCharter pc, Map<String, Object> r) {

        final ImportCharterRequest.PlacingCharter.ContractObject co = (ImportCharterRequest.PlacingCharter.ContractObject) DB.to.javaBean (ImportCharterRequest.PlacingCharter.ContractObject.class, r);
        
        if (Boolean.FALSE.equals (co.isIsManagedByContract ())) co.setIsManagedByContract (null);
        
        co.setTransportGUID (UUID.randomUUID ().toString ());
        
        co.setBaseMService (CharterFile.getBaseServiceType (r));
        
        for (Map<String, Object> service: (List<Map<String, Object>>) r.get ("services")) CharterObjectService.add (co, service);

        pc.getContractObject ().add (co);                        
        
    }
    
    public static void add (ImportCharterRequest.EditCharter ec, Map<String, Object> r) {
        
        final ImportCharterRequest.EditCharter.ContractObject co = (ImportCharterRequest.EditCharter.ContractObject) DB.to.javaBean (ImportCharterRequest.EditCharter.ContractObject.class, r);
                
        co.setTransportGUID (UUID.randomUUID ().toString ());
        
        if (r.get ("contractobjectversionguid") == null) {
            
            final ImportCharterRequest.EditCharter.ContractObject.Add add = (ImportCharterRequest.EditCharter.ContractObject.Add) DB.to.javaBean (ImportCharterRequest.EditCharter.ContractObject.Add.class, r);
            
            if (Boolean.FALSE.equals (add.isIsManagedByContract ())) add.setIsManagedByContract (null);
        
            add.setBaseMService (CharterFile.getBaseServiceType (r));
            
            for (Map<String, Object> service: (List<Map<String, Object>>) r.get ("services")) CharterObjectService.add (add, service);
            
            co.setAdd (add);
        
        }
        else {
            
            final Object nul = r.get ("annulmentinfo");
            
            if (nul != null && !nul.toString ().isEmpty ()) {

                final ImportCharterRequest.EditCharter.ContractObject.Annulment annulment = new ImportCharterRequest.EditCharter.ContractObject.Annulment ();

                annulment.setContractObjectVersionGUID (r.get ("contractobjectversionguid").toString ());

                co.setAnnulment (annulment);

            }
            else {

                ImportCharterRequest.EditCharter.ContractObject.Edit ed = (ImportCharterRequest.EditCharter.ContractObject.Edit) DB.to.javaBean (ImportCharterRequest.EditCharter.ContractObject.Edit.class, r);

                if (Boolean.FALSE.equals (ed.isIsManagedByContract ())) ed.setIsManagedByContract (null);
            
                ed.setBaseMService (CharterFile.getBaseServiceType (r));

                for (Map<String, Object> service: (List<Map<String, Object>>) r.get ("services")) CharterObjectService.add (ed, service);

                co.setEdit (ed);

            }        
            
        }
        
        ec.getContractObject ().add (co);
        
    }

    private final static String [] keyFields = {"fiashouseguid", "startdate"};

    public class Sync extends SyncMap<ExportCAChResultType.Charter.ContractObject>{

        UUID uuid_contract;
        CharterFile.Sync files;

        public Sync (DB db, UUID uuid_contract, CharterFile.Sync files) {
            super (db);
            this.uuid_contract = uuid_contract;
            this.files = files;
            commonPart.put ("uuid_charter", uuid_contract);
            commonPart.put ("is_deleted", 0);
        }

        @Override
        public String[] getKeyFields () {
            return keyFields;
        }

        @Override
        public void setFields (Map<String, Object> h, ExportCAChResultType.Charter.ContractObject co) {

            byte status = VocGisStatus.i.forName (co.getStatusObject ().value ()).getId ();

            h.put ("fiashouseguid", co.getFIASHouseGuid ());
            h.put ("startdate", co.getStartDate ());
            h.put ("enddate", co.getEndDate ());
            h.put ("uuid_charter_file", files.getPk (co.getBaseMService ().getAgreement ()));
            h.put ("id_ctr_status_gis", status);
            h.put ("id_ctr_status", status);
            h.put ("is_from_gis", 1);
            h.put ("contractobjectversionguid", co.getContractObjectVersionGUID ());

        }

        @Override
        public Table getTable () {
            return CharterObject.this;
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

    }
    
}