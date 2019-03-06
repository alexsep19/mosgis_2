package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class BankAccount extends EnTable {

    public enum c implements EnColEnum {
        
        UUID_ORG               (VocOrganization.class, null, "Организация-владелец счёта"),
	BIKCREDORG             (VocBic.class,          null, "БИК"),
        
        ACCOUNTNUMBER          (Type.STRING,  20,      null, "Номер счёта"),
        
        OPENDATE               (Type.DATE,             null,  "Дата открытия/изменения реквизитов"),
        CLOSEDATE              (Type.DATE,             null,  "Дата закрытия"),
        
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        @Override
        public boolean isLoggable () {            
            switch (this) {
//                case ID_LOG:
//                    return false;
                default:
                    return true;
            }
        }        

    }

    public BankAccount () {
        
        super  ("tb_bnk_accts", "Платёжные реквизиты (расчётные счета)");
        
        cols   (c.class);        
        
        key ("uuid_org", "uuid_org");

/*        
        trigger ("BEFORE UPDATE", 
                
            "DECLARE "
            + " cnt NUMBER;"
            + "BEGIN "
                    
                + "IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS "
                    + " AND :OLD.ID_CTR_STATUS <> " + VocGisStatus.i.FAILED_PLACING
                    + " AND :NEW.ID_CTR_STATUS =  " + VocGisStatus.i.PROJECT
                + " THEN "
                    + " :NEW.ID_CTR_STATUS := " + VocGisStatus.i.MUTATING
                + "; END IF; "
                        
                + "IF :NEW.is_deleted=0 AND :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS AND :NEW.ID_CTR_STATUS=" + VocGisStatus.i.PENDING_RQ_PLACING + " THEN BEGIN "
                        
                + "SELECT COUNT(*) INTO cnt FROM tb_account_items WHERE is_deleted=0 AND uuid_account=:NEW.uuid; "
                + "IF cnt=0 THEN raise_application_error (-20000, 'Для данного счёта не указано ни одно помещение. Операция отменена.'); END IF; "
                        
                + " FOR i IN ("
                    + "SELECT "
                    + " o.uuid "
                    + " , h.address "
                    + " , p.label "
                    + "FROM "
                    + " tb_account_items o "
                    + " LEFT JOIN tb_houses h ON o.fiashouseguid = h.fiashouseguid"
                    + " LEFT JOIN vw_premises p ON o.uuid_premise = p.id"
                    + " WHERE o.is_deleted = 0"
                    + " AND o.uuid_account = :NEW.uuid "
                    + " AND p.id IS NOT NULL AND p.premisesguid IS NULL AND p.livingroomguid IS NULL"
                    + ") LOOP"
                + " raise_application_error (-20000, 'Помещение ' || i.address || ', ' || i.label || ' не размещено в ГИС ЖКХ. Операция отменена.'); "
                + " END LOOP; "
                        
                + " FOR i IN ("
                    + "SELECT "
                    + " o.uuid "
                    + " , h.address "
                    + "FROM "
                    + " tb_account_items o "
                    + " LEFT JOIN tb_houses h ON o.fiashouseguid = h.fiashouseguid"
                    + " WHERE o.is_deleted = 0"
                    + " AND o.uuid_account = :NEW.uuid "
                    + " AND h.gis_guid IS NULL "
                    + ") LOOP"
                + " raise_application_error (-20000, 'Паспорт МКД с адресом ' || i.address || ' не размещён в ГИС ЖКХ. Операция отменена.'); "
                + " END LOOP; "                        

                + "END; END IF; "

                + "IF "
                    + "     :OLD.ID_CTR_STATUS = " + VocGisStatus.i.MUTATING
                    + " AND :NEW.ID_CTR_STATUS = " + VocGisStatus.i.PENDING_RQ_PLACING
                + " THEN "
                    + " :NEW.ID_CTR_STATUS := " + VocGisStatus.i.PENDING_RQ_EDIT
                + "; END IF; "
                        
            + "END;"
                
        );
*/
    }

}