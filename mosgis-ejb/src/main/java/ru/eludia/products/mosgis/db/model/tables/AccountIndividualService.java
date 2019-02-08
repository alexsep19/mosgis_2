package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.AttachTable;

public class AccountIndividualService extends AttachTable {
    
    public enum c implements EnColEnum {

        UUID_ACCOUNT           (Account.class,           "Лицевой счёт"),
        
        UUID_ADD_SERVICE       (AdditionalService.class, "Дополнительная услуга"),
        BEGINDATE              (DATE,                    "Дата начала представления услуги"),
        ENDDATE                (DATE,                    "Дата окончания представления услуги"),

//        ID_LOG               (AccountIndividualServiceLog.class,  "Последнее событие редактирования")
        
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            return false;
        }

    }    

    public AccountIndividualService () {
        
        super  ("tb_account_svc", "Индивидуальные услуги лицевых счетов");
        
        cols   (c.class);
        
        key    ("parent", c.UUID_ACCOUNT);        
        key    ("attachmentguid", AttachTable.c.ATTACHMENTGUID);
       
        trigger ("BEFORE UPDATE", "BEGIN "
                
//            + " IF :NEW.len IS NULL THEN :NEW.len := -1; END IF; "                
/*
            + "IF (NVL (:OLD.attachmentguid, '00') = NVL (:NEW.attachmentguid, '00')) THEN BEGIN"
            + " UPDATE tb_charter_files__log SET attachmentguid = :NEW.attachmentguid, attachmenthash = :NEW.attachmenthash WHERE uuid = :NEW.id_log; "
            + "END; END IF; "
*/
            + CHECK_LEN

        + "END;");        

    }
    
}