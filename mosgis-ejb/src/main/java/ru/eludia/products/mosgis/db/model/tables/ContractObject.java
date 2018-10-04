package ru.eludia.products.mosgis.db.model.tables;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import static ru.eludia.products.mosgis.db.model.voc.VocGisCustomerType.i.OWNERS;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.gosuslugi.dom.schema.integration.house_management.ExportStatusCAChResultType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportContractRequest;

public class ContractObject extends Table {

    public ContractObject () {
        
        super  ("tb_contract_objects", "Объекты договоров управления");
        
        pk     ("uuid",                    Type.UUID,             NEW_UUID,     "Ключ");
        col    ("is_deleted",              Type.BOOLEAN,          Bool.FALSE,   "1, если запись удалена; иначе 0");
        
        ref    ("uuid_contract",           Contract.class,                      "Ссылка на договор");
        ref    ("uuid_contract_agreement", ContractFile.class,    null,         "Ссылка на дополнительное соглашение");
        
        fk     ("fiashouseguid",           VocBuilding.class,                   "Глобальный уникальный идентификатор дома по ФИАС");

        col    ("startdate",               Type.DATE,                           "Дата начала предоставления услуг");
        col    ("enddate",                 Type.DATE,                           "Дата окончания предоставления услуг");        
       
        col    ("annulmentinfo",           Type.STRING,           null,       "Причина аннулирования.");
        col    ("is_annuled",              Type.BOOLEAN,          new Virt ("DECODE(\"ANNULMENTINFO\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");
        
        fk     ("id_ctr_status",           VocGisStatus.class,                  new Num (VocGisStatus.i.PROJECT.getId ()), "Статус объекта договора с точки зрения mosgis");
        fk     ("id_ctr_status_gis",       VocGisStatus.class,                  new Num (VocGisStatus.i.PROJECT.getId ()), "Статус объекта договора с точки зрения ГИС ЖКХ");

        col    ("contractobjectversionguid",     Type.UUID,       null,          "UUID последней версии данного объекта в ГИС ЖКХ");
        
        fk     ("id_log",                  ContractObjectLog.class,  null,      "Последнее событие редактирования");
 
        trigger ("BEFORE INSERT OR UPDATE", ""
                
            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "
                
            + "IF INSERTING"
                + " OR (NVL (:OLD.uuid_contract_agreement, '00') <> NVL (:NEW.uuid_contract_agreement, '00'))"
                + " OR (NVL (:OLD.annulmentinfo, CHR(0)) <> NVL (:NEW.annulmentinfo, CHR(0)))"
                + " OR (:OLD.startdate <> :NEW.startdate)"
                + " OR (:OLD.enddate   <> :NEW.enddate)"
            + " THEN "
            + " FOR i IN (SELECT uuid FROM tb_contracts WHERE uuid=:NEW.uuid_contract AND id_ctr_status NOT IN (10, 11)) LOOP"
            + "   raise_application_error (-20000, 'Внесение изменений в договор в настоящее время запрещено. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "

            + "IF INSERTING THEN "
            + " FOR i IN (SELECT c.uuid FROM tb_contracts c INNER JOIN tb_contract_objects o ON (o.uuid_contract = c.uuid AND o.uuid <> :NEW.uuid AND o.is_deleted = 0 AND o.is_annuled = 0) WHERE c.uuid=:NEW.uuid_contract AND c.id_customer_type=" + OWNERS.getId () + ") LOOP"
            + "   raise_application_error (-20000, 'Поскольку заказчик — собственник объекта жилищного фонда, объект в договоре может быть только один. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "
                
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
                + " AND o.enddate   >= :NEW.startdate "
                + " AND o.startdate <= :NEW.enddate "
                + " AND o.uuid <> NVL(:NEW.uuid, '00') "
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
                
            + "IF :OLD.is_deleted = 0 AND :NEW.is_deleted = 1 THEN "
            + " UPDATE tb_contract_services SET is_deleted = 1 WHERE uuid_contract_object = :NEW.uuid; "
            + " COMMIT; "
            + "END IF; "

            + "IF UPDATING AND (NVL (:NEW.contractobjectversionguid, '00') <> NVL (:NEW.contractobjectversionguid, '00')) THEN "
            + " UPDATE tb_contract_objects__log SET contractobjectversionguid = :NEW.contractobjectversionguid WHERE uuid = :NEW.id_log; "
            + " COMMIT; "
            + "END IF; "

//            + "IF :OLD.contractobjectversionguid IS NOT NULL THEN :NEW.id_ctr_status := " + MUTATING.getId () + "; END IF; "
                    
        + "END;");

    }

    public static void add (ImportContractRequest.Contract.PlacingContract pc, Map<String, Object> r) {

        final ImportContractRequest.Contract.PlacingContract.ContractObject co = (ImportContractRequest.Contract.PlacingContract.ContractObject) DB.to.javaBean (ImportContractRequest.Contract.PlacingContract.ContractObject.class, r);
        
        co.setTransportGUID (UUID.randomUUID ().toString ());

        co.setBaseMService (ContractFile.getBaseServiceType (r));
        
        for (Map<String, Object> service: (List<Map<String, Object>>) r.get ("services")) ContractObjectService.add (co, service);

        pc.getContractObject ().add (co);        

    }
    
    private static Logger logger = java.util.logging.Logger.getLogger (ContractObject.class.getName ());
    
    public static void add (ImportContractRequest.Contract.EditContract ec, Map<String, Object> r) {
        
        final ImportContractRequest.Contract.EditContract.ContractObject co = (ImportContractRequest.Contract.EditContract.ContractObject) DB.to.javaBean (ImportContractRequest.Contract.EditContract.ContractObject.class, r);
        
        co.setTransportGUID (UUID.randomUUID ().toString ());
        
logger.info ("r=" + r);
        
        if (r.get ("contractobjectversionguid") == null) {
            
            final ImportContractRequest.Contract.EditContract.ContractObject.Add add = (ImportContractRequest.Contract.EditContract.ContractObject.Add) DB.to.javaBean (ImportContractRequest.Contract.EditContract.ContractObject.Add.class, r);
            
            add.setBaseMService (ContractFile.getBaseServiceType (r));
            
            for (Map<String, Object> service: (List<Map<String, Object>>) r.get ("services")) ContractObjectService.add (add, service);            
            
            co.setAdd (add);
        
        }
        else {
            
            final Object nul = r.get ("annulmentinfo");
            
            if (nul != null && !nul.toString ().isEmpty ()) {

                final ImportContractRequest.Contract.EditContract.ContractObject.Annulment annulment = new ImportContractRequest.Contract.EditContract.ContractObject.Annulment ();

                annulment.setContractObjectVersionGUID (r.get ("contractobjectversionguid").toString ());

                co.setAnnulment (annulment);

            }
            else {

                ImportContractRequest.Contract.EditContract.ContractObject.Edit ed = (ImportContractRequest.Contract.EditContract.ContractObject.Edit) DB.to.javaBean (ImportContractRequest.Contract.EditContract.ContractObject.Edit.class, r);

                ed.setBaseMService (ContractFile.getBaseServiceType (r));

                for (Map<String, Object> service: (List<Map<String, Object>>) r.get ("services")) ContractObjectService.add (ed, service);

                co.setEdit (ed);

            }        
            
        }
        
        ec.getContractObject ().add (co);
        
    }    
    
    public QP updateStatus (UUID uuid_contract, ExportStatusCAChResultType.ContractObject co) {
    
        QP qp = new QP ("UPDATE ");
                    
        qp.append (getName ());
        qp.add (" SET id_ctr_status_gis         = ?", VocGisStatus.i.forName (co.getManagedObjectStatus ().value ()).getId (), getColumn ("id_ctr_status_gis").toPhysical ());
        qp.add (",    contractobjectversionguid = ?", co.getContractObjectVersionGUID (),                                      getColumn ("contractobjectversionguid").toPhysical ());

        qp.append (" WHERE is_deleted = 0");
        qp.add (" AND uuid_contract   = ?", uuid_contract,          getColumn ("uuid_contract").toPhysical ());
        qp.add (" AND fiashouseguid   = ?", co.getFIASHouseGuid (), getColumn ("fiashouseguid").toPhysical ());
                    
        return qp;
    
    }

}