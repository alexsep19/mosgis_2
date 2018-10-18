package ru.eludia.products.mosgis.db.model.tables;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocCharterObjectReason;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.gosuslugi.dom.schema.integration.house_management.ImportCharterRequest;

public class CharterObject extends Table {

    public CharterObject () {
        
        super  ("tb_charter_objects", "Объекты уставов");
        
        pk     ("uuid",                    Type.UUID,             NEW_UUID,     "Ключ");
        col    ("is_deleted",              Type.BOOLEAN,          Bool.FALSE,   "1, если запись удалена; иначе 0");
        
        ref    ("uuid_charter",           Charter.class,                      "Ссылка на договор");
        
        fk     ("fiashouseguid",           VocBuilding.class,                   "Глобальный уникальный идентификатор дома по ФИАС");
        
        fk     ("id_reason",               VocCharterObjectReason.class, new Num (VocCharterObjectReason.i.CHARTER.getId ()), "Основание");
        ref    ("uuid_charter_file",       CharterFile.class,     null,          "Ссылка на протокол");
        col    ("ismanagedbycontract",     Type.BOOLEAN,          Bool.FALSE,   "Управление многоквартирным домом осуществляется управляющей организацией по договору управления");

        col    ("startdate",               Type.DATE,                           "Дата начала предоставления услуг");
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
                
            + "IF :NEW.is_deleted = 0 THEN "
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

                    
            + "IF :NEW.is_deleted = 0 THEN "
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

                ed.setBaseMService (CharterFile.getBaseServiceType (r));

                for (Map<String, Object> service: (List<Map<String, Object>>) r.get ("services")) CharterObjectService.add (ed, service);

                co.setEdit (ed);

            }        
            
        }
        
        ec.getContractObject ().add (co);
        
    }
    
}