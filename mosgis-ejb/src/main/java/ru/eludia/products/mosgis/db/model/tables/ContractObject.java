package ru.eludia.products.mosgis.db.model.tables;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
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
        
        fk     ("id_ctr_status",           VocGisStatus.class,                  new Num (VocGisStatus.i.PROJECT.getId ()), "Статус объекта договора с точки зрения mosgis");
        fk     ("id_ctr_status_gis",       VocGisStatus.class,                  new Num (VocGisStatus.i.PROJECT.getId ()), "Статус объекта договора с точки зрения ГИС ЖКХ");

        col    ("contractobjectversionguid",     Type.UUID,      null,          "UUID последней версии данного объекта в ГИС ЖКХ");
        
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
                
            + "IF :NEW.is_deleted = 1 THEN "
            + " UPDATE tb_contract_services SET is_deleted = 1 WHERE uuid_contract_object = :NEW.uuid; "
            + "END IF; "

        + "END;");

    }

    public static void add (ImportContractRequest.Contract.PlacingContract pc, Map<String, Object> r) {

        final ImportContractRequest.Contract.PlacingContract.ContractObject co = (ImportContractRequest.Contract.PlacingContract.ContractObject) DB.to.javaBean (ImportContractRequest.Contract.PlacingContract.ContractObject.class, r);
        
        co.setTransportGUID (UUID.randomUUID ().toString ());

        co.setBaseMService (ContractFile.getBaseServiceType (r));
        
        for (Map<String, Object> service: (List<Map<String, Object>>) r.get ("services")) ContractObjectService.add (co, service);

        pc.getContractObject ().add (co);        

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