package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;

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

        + "END;");

    }
    
}