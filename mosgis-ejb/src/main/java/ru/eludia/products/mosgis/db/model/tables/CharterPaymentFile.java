package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.AttachTable;

public class CharterPaymentFile extends AttachTable {
    
    public enum c implements EnColEnum {

        UUID_CHARTER_PAYMENT (CharterPayment.class,    "Ссылка на объект договора"),
        ID_LOG               (CharterPaymentFileLog.class,  "Последнее событие редактирования")
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

    public CharterPaymentFile () {
        
        super  ("tb_ch_pay_files", "Файлы, приложенные к сведениям о размере платы за] услуги управления");
        
        cols   (c.class);
        
        key    ("parent", c.UUID_CHARTER_PAYMENT);        
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